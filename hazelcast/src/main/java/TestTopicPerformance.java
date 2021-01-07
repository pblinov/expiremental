import com.hazelcast.core.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author pblinov
 * @since 13/03/2015
 */
public class TestTopicPerformance {
    private static final Logger LOGGER = Logger.getLogger(TestTopicPerformance.class.getName());

    private static final int COUNTER = 1_000_000;

    public static void main(String [] args) {
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance();

        final String TOPIC = "TestMessages";

        final long start = System.currentTimeMillis();

        final ITopic<String> topic = h1.getTopic( TOPIC );

        final CountDownLatch latch = new CountDownLatch(COUNTER);
        topic.addMessageListener(message -> {
            LOGGER.info("M: " + message.getMessageObject());
            latch.countDown();
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            final long start1 = System.currentTimeMillis();
            for (int i = 0; i < COUNTER + 1; i++) {
                topic.publish(TestHazelcast.name(i));
            }
            LOGGER.info("PUB: " + (System.currentTimeMillis() - start1) / (float) (COUNTER));
        });

        try {
            latch.await();
        } catch ( InterruptedException ignored ) {
        }

        LOGGER.info("END: " + (System.currentTimeMillis() - start) / (float) (COUNTER));

        h1.shutdown();
    }
}
