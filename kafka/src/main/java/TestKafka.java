import java.util.logging.Logger;


/**
 * @author pblinov
 * @since 18/02/2015
 */
public class TestKafka {
    private static final Logger LOGGER = Logger.getLogger(TestKafka.class.getName());

    public static void main(String [] args) {
        LOGGER.info("Starting...");

//        Producer producerThread = new Producer(KafkaProperties.topic);
//        producerThread.start();

        Consumer consumerThread = new Consumer(KafkaProperties.topic);
        consumerThread.start();

        LOGGER.info("Stop");
    }
}
