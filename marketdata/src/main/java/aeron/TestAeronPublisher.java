package aeron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zmq.TestZmqPub;

import java.util.stream.IntStream;

/**
 * @author pblinov
 * @since 29/09/2017
 */
public class TestAeronPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAeronPublisher.class);
    public static final String CHANNEL = "aeron:udp?endpoint=localhost:40123";
    public static final int STREAM = 10;

    public static void main(String[] args) throws InterruptedException {
        LOGGER.info("Start");
        AeronPublisherTransport transport = new AeronPublisherTransport(CHANNEL, STREAM);
        transport.start();

        IntStream.range(1, 5_000_000).forEach(i -> {
            transport.send(TestZmqPub.generateQuote(i));
        });

        Thread.sleep(600000L);

        LOGGER.info("Stop");
    }
}
