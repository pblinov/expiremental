package disruptor;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;

public class DisruptorTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DisruptorTest.class);

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Start");
        ThreadFactory threadFactory = DaemonThreadFactory.INSTANCE;

        WaitStrategy waitStrategy = new BusySpinWaitStrategy();
        Disruptor<QuoteValue> disruptor
                = new Disruptor<>(
                QuoteValue.EVENT_FACTORY,
                16,
                threadFactory,
                ProducerType.SINGLE,
                waitStrategy);

        disruptor.handleEventsWith((event, sequence, endOfBatch) -> LOGGER.info("{}: {}-{}", event.getValue(), sequence, endOfBatch));

        RingBuffer<QuoteValue> ringBuffer = disruptor.start();

        for (int eventCount = 0; eventCount < 32; eventCount++) {
            long sequenceId = ringBuffer.next();
            QuoteValue valueEvent = ringBuffer.get(sequenceId);
            valueEvent.setValue(eventCount);
            ringBuffer.publish(sequenceId);
        }

        Thread.sleep(1000);

        LOGGER.info("Stop");
    }
}
