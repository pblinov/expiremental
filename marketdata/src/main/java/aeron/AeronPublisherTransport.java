package aeron;

import marketdata.QuoteSerializer;
import marketdata.transport.PublisherTransport;
import marketdata.transport.Quote;
import org.agrona.BufferUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.real_logic.aeron.Aeron;
import uk.co.real_logic.aeron.Publication;
import uk.co.real_logic.aeron.driver.MediaDriver;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

import static uk.co.real_logic.aeron.Publication.*;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class AeronPublisherTransport implements PublisherTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(AeronPublisherTransport.class);

    private MediaDriver driver;
    private Aeron.Context context;
    private String channel;
    private int stream;
    private final UnsafeBuffer BUFFER = new UnsafeBuffer(BufferUtil.allocateDirectAligned(256, 64));
    private Aeron aeron;
    private Publication publication;
    private QuoteSerializer quoteSerializer = new QuoteSerializer();

    public AeronPublisherTransport(String channel, int stream) {
        this.channel = channel;
        this.stream = stream;
        driver = MediaDriver.launchEmbedded();
        context = new Aeron.Context();
        context.aeronDirectoryName(driver.aeronDirectoryName());
    }

    @Override
    public void send(Quote quote) {
        byte[] message = quoteSerializer.serialize(quote);
        BUFFER.putBytes(0, message);
        long result = 0;
        do {
            result = publication.offer(BUFFER, 0, message.length);
        } while (result == BACK_PRESSURED);

        if (result < 0) {
            LOGGER.error("Cannot send: {}", format(result));
        }

        if (!publication.isConnected())
        {
            LOGGER.warn("No active subscribers detected");
        }
    }

    private String format(long result) {
        if (result == NOT_CONNECTED) {
            return "not connected";
        } else if (result == BACK_PRESSURED) {
            return "back pressured";
        } else if (result == ADMIN_ACTION) {
            return "admin action";
        } else if (result == CLOSED) {
            return "closed";
        }
        return "unknown";
    }

    public void start() {
        aeron = Aeron.connect(context);
        publication = aeron.addPublication(channel, stream);
    }

    public void stop() {

    }
}
