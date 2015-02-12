import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main {

    public static final int QUEUE_SIZE = 256;
    public static final int THREADS = 2;
    public static final int EVENTS_COUNTER = 1_000;

    public static void main(String[] args) throws IOException {

        long blockingQueueDuration = blockingQueue();
        long disruptorDuration = disruptor();
        System.out.println(String.format("%s/%s ms", disruptorDuration, blockingQueueDuration));
    }

    private static long disruptor() throws IOException {
        Disruptor<Event> disruptor = new Disruptor<>(new Factory(), QUEUE_SIZE, Executors.newFixedThreadPool(THREADS));
        disruptor.handleEventsWith(new Handler());
        disruptor.start();

        RingBuffer<Event> ringBuffer = disruptor.getRingBuffer();

        final long start = System.currentTimeMillis();

        for (int i = 0; i < EVENTS_COUNTER; i++) {
            long seq = ringBuffer.next();
            try {
                Event event = ringBuffer.get(seq);
                event.set(i);
            } finally {
                ringBuffer.publish(seq);
            }
        }

        disruptor.halt();

        return System.currentTimeMillis() - start;
    }

    private static long blockingQueue() throws IOException {
        BlockingQueue<Event> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        Executor executor = Executors.newFixedThreadPool(THREADS);


        final long start = System.currentTimeMillis();

        for (int i = 0; i < THREADS; i++) {
            executor.execute(() -> {
                try {
                    while (true) {
                        SlowAction.doSomething(queue.take());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        for (int i = 0; i < EVENTS_COUNTER; i++) {
            Event event = new Event();
            event.set(i);
            try {
                queue.put(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return System.currentTimeMillis() - start;
    }
}
