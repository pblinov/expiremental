import com.hazelcast.core.*;
import com.hazelcast.ringbuffer.Ringbuffer;
import com.hazelcast.topic.ReliableMessageListener;
import com.hazelcast.topic.TopicOverloadPolicy;
import com.hazelcast.topic.impl.reliable.ReliableMessageListenerAdapter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.hazelcast.ringbuffer.impl.RingbufferService.TOPIC_RB_PREFIX;

/**
 * @author pblinov
 * @since 13/03/2015
 */
public class TestReliableTopic {
    private static final Logger LOGGER = Logger.getLogger(TestReliableTopic.class.getName());

    private static final int COUNTER = 100;

    public static void main(String [] args) {
        HazelcastInstance h1 = Hazelcast.newHazelcastInstance();
        h1.getConfig().getReliableTopicConfig("*").setTopicOverloadPolicy(TopicOverloadPolicy.DISCARD_OLDEST);
        h1.getConfig().getRingbufferConfig("*").setCapacity(10);
        h1.getConfig().getRingbufferConfig("*").setTimeToLiveSeconds(30);

        final String TOPIC = "TestMessages";

        final ITopic<String> topic1 = h1.getReliableTopic(TOPIC);
        topic1.destroy();

        final ITopic<String> topic = h1.getReliableTopic(TOPIC);
        final Ringbuffer<String> rb = h1.getRingbuffer(TOPIC_RB_PREFIX + TOPIC);

        LOGGER.info("START");

        for (int i = 0; i < COUNTER + 1; i++) {
            topic.publish(TestHazelcast.name(i));
        }

        final CountDownLatch latch = new CountDownLatch(COUNTER);

        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                topic.publish("AAA");


                LOGGER.info("LISTEN");

                MessageListener<String> messageListener = new MessageListener<String>() {
                    @Override
                    public void onMessage(Message<String> message) {
                        LOGGER.info("RECEIVED: " + message.getMessageObject());
                        //message.getPublishTime()
                        latch.countDown();
                    }
                };
                ReliableMessageListenerAdapter<String> reliableMessageListener = new ReliableMessageListenerAdapter<String>(messageListener) {
                    @Override
                    public long retrieveInitialSequence() {
                        return 0;//rb.tailSequence() - 5;
                    }

                    @Override
                    public boolean isLossTolerant() {
                        return true;
                    }

                    @Override
                    public boolean isTerminal(Throwable failure) {
                        LOGGER.severe(failure.getMessage());
                        return false;
                    }
                };
                topic.addMessageListener(reliableMessageListener);

                topic.publish("BBB");
            }
        }, 40, TimeUnit.SECONDS);



        LOGGER.info("WAIT");
        try {
            latch.await();
        } catch ( InterruptedException ignored ) {
        }

        h1.shutdown();
    }
}
