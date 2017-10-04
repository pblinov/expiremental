package aeron;

import org.agrona.BufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Publication;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;
import zmq.TestZmqPub;

import java.util.concurrent.TimeUnit;
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

            IntStream.range(1, 1_000_000).forEach(i -> {
                transport.send(TestZmqPub.generateQuote(i));

//                try {
//                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
//                } catch (InterruptedException e) {
//                    LOGGER.error("Cannot sleep", e);
//                }
            });

        Thread.sleep(600000L);

        LOGGER.info("Stop");
    }
}
