import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main {
    public static final int QUEUE_SIZE = 256;
    public static final int THREADS = 1;
    public static final int EVENTS_COUNTER = 30_000;

    public static void main(String[] args) throws IOException, InterruptedException {
        long blockingQueueDuration = blockingQueue();
        long disruptorDuration = disruptor();
        System.out.println(String.format("Disruptor:\t%s ms\nQueue:\t%s ms", disruptorDuration, blockingQueueDuration));
    }

    private static long disruptor() throws IOException, InterruptedException {
        CountDownLatch latch = new CountDownLatch(EVENTS_COUNTER);

        ExecutorService executor = createExecutor();

        Disruptor<Event> disruptor = new Disruptor<>(new Factory(), QUEUE_SIZE, executor);
        disruptor.handleEventsWith(new Handler(latch));
        disruptor.start();

        RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();

        final long start = System.currentTimeMillis();

        IntStream.range(0, EVENTS_COUNTER).forEach(i -> {
            long seq = ringBuffer.next();
            try {
                Event event = ringBuffer.get(seq);
                event.set(i);
            } finally {
                ringBuffer.publish(seq);
            }
        });

        latch.await();
        disruptor.halt();

        return System.currentTimeMillis() - start;
    }

    private static long blockingQueue() throws IOException, InterruptedException {
        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        Executor executor = createExecutor();

        final long start = System.currentTimeMillis();

        CountDownLatch latch = new CountDownLatch(EVENTS_COUNTER);

        IntStream.range(0, THREADS).forEach(i -> {
            executor.execute(() -> {
                try {
                    while (true) {
                        SlowAction.doSomething(queue.take());
                        latch.countDown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        });

        IntStream.range(0, EVENTS_COUNTER).forEach(i -> {
            Event event = new Event();
            event.set(i);
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        latch.await();

        return System.currentTimeMillis() - start;
    }

    private static ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(THREADS);
    }
}
