package aeron;

import org.agrona.BufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Publication;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

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
    private static final UnsafeBuffer BUFFER = new UnsafeBuffer(BufferUtil.allocateDirectAligned(256, 64));

    public static void main(String[] args) {
        LOGGER.info("Start");

        final MediaDriver driver = MediaDriver.launchEmbedded();
        final Aeron.Context context = new Aeron.Context();
        context.aeronDirectoryName(driver.aeronDirectoryName());

        try (Aeron aeron = Aeron.connect(context);
             Publication publication = aeron.addPublication(CHANNEL, STREAM)) {
            IntStream.range(1, 5_000_000).mapToObj(Integer::toString).forEach(i -> {
                final String message = "Hello World! " + i;
                final byte[] messageBytes = message.getBytes();
                BUFFER.putBytes(0, messageBytes);
                final long result = publication.offer(BUFFER, 0, messageBytes.length);
                if (result < 0) {
                    LOGGER.error("Cannot send: {}", result);
                }

                if (!publication.isConnected())
                {
                    LOGGER.warn("No active subscribers detected");
                }

//                try {
//                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
//                } catch (InterruptedException e) {
//                    LOGGER.error("Cannot sleep", e);
//                }
            });
        }

        LOGGER.info("Stop");
    }
}
