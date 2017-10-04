package aeron;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.aeron.logbuffer.FragmentHandler;
import uk.co.real_logic.agrona.CloseHelper;
import uk.co.real_logic.agrona.concurrent.SigInt;
import zmq.TestZmqSub;

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
        AeronSubscriberTransport transport = new AeronSubscriberTransport(CHANNEL, STREAM);
        transport.addListener(TestZmqSub.getQuoteListener(System.currentTimeMillis()));
        transport.start();
    }
}
