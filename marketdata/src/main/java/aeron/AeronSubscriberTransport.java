package aeron;

import marketdata.QuoteSerializer;
import marketdata.transport.QuoteListener;
import marketdata.transport.SubscriberTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Subscription;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.aeron.logbuffer.FragmentHandler;
import uk.co.real_logic.agrona.concurrent.SigInt;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class AeronSubscriberTransport implements SubscriberTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(AeronSubscriberTransport.class);

    private MediaDriver driver;
    private Aeron.Context context;
    private Aeron aeron;
    private Subscription subscription;
    private QuoteSerializer quoteSerializer = new QuoteSerializer();
    private String channel;
    private int stream;
    private QuoteListener listener;
    private FragmentHandler fragmentHandler;

    public AeronSubscriberTransport(String channel, int stream) {
        this.channel = channel;
        this.stream = stream;
        driver = MediaDriver.launchEmbedded();
        context = new Aeron.Context();
        context.aeronDirectoryName(driver.aeronDirectoryName());
    }

    @Override
    public void addListener(QuoteListener listener) {
        this.listener = listener;
    }

    @Override
    public void subscribe(String symbol) {

    }

    @Override
    public void unsubscribe(String symbol) {

    }

    public void start() {
        fragmentHandler =
                (buffer, offset, length, header) -> {
                    final byte[] data = new byte[length];
                    buffer.getBytes(offset, data);
                    if (listener != null) {
                        try {
                            listener.onQuote(quoteSerializer.deserialize(data));
                        } catch (Exception e) {
                            LOGGER.error("Cannot deserialize: " + data.length, e);
                        }
                    }
                };
        aeron = Aeron.connect(context);
        subscription = aeron.addSubscription(channel, stream);
        final AtomicBoolean running = new AtomicBoolean(true);
        SigInt.register(() -> running.set(false));
        SamplesUtil.subscriberLoop(fragmentHandler, 20, running).accept(subscription);
    }

    public void stop() {

    }
}
