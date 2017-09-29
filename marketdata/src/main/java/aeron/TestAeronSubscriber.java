package aeron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.aeron.logbuffer.FragmentHandler;
import uk.co.real_logic.agrona.CloseHelper;
import uk.co.real_logic.agrona.concurrent.SigInt;

import java.util.concurrent.atomic.AtomicBoolean;

import static aeron.TestAeronPublisher.CHANNEL;
import static aeron.TestAeronPublisher.STREAM;

/**
 * @author pblinov
 * @since 29/09/2017
 */
public class TestAeronSubscriber {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestAeronSubscriber.class);

    public static void main(String[] args) {
        final MediaDriver driver = MediaDriver.launchEmbedded();
        final Aeron.Context context = new Aeron.Context();
        context.aeronDirectoryName(driver.aeronDirectoryName());

        final AtomicBoolean running = new AtomicBoolean(true);

        // Register a SIGINT handler for graceful shutdown.
        SigInt.register(() -> running.set(false));

        // dataHandler method is called for every new datagram received
        final FragmentHandler fragmentHandler =
                (buffer, offset, length, header) ->
                {
                    final byte[] data = new byte[length];
                    buffer.getBytes(offset, data);

                    System.out.println(String.format(
                            "Received message (%s) to stream %d from session %x term id %x term offset %d (%d@%d)",
                            new String(data), STREAM, header.sessionId(),
                            header.termId(), header.termOffset(), length, offset));

                    // Received the intended message, time to exit the program
                    //running.set(false);
                };

        // Create an Aeron instance using the configured Context and create a
        // Subscription on that instance that subscribes to the configured
        // channel and stream ID.
        // The Aeron and Subscription classes implement "AutoCloseable" and will automatically
        // clean up resources when this try block is finished
        try (Aeron aeron = Aeron.connect(context);
             Subscription subscription = aeron.addSubscription(CHANNEL, STREAM))
        {
            SamplesUtil.subscriberLoop(fragmentHandler, 20, running).accept(subscription);

            System.out.println("Shutting down...");
        }

        CloseHelper.quietClose(driver);
    }
}
