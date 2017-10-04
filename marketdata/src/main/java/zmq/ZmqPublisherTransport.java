package zmq;

import marketdata.QuoteSerializer;
import marketdata.transport.PublisherTransport;
import marketdata.transport.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class ZmqPublisherTransport implements PublisherTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZmqPublisherTransport.class);

    private volatile boolean started;
    private String address;
    private ZMQ.Socket publisher;
    private QuoteSerializer quoteSerializer = new QuoteSerializer();

    public ZmqPublisherTransport(String address, int hwmSize) {
        this.address = address;
        ZMQ.Context ctx = ZMQ.context(1);
        publisher = ctx.socket(ZMQ.PUB);
        publisher.setHWM(hwmSize);
    }

    @Override
    public void send(Quote quote) {
        byte[] message = quoteSerializer.serialize(quote);
        publisher.sendMore(quote.getSymbol());
        publisher.send(message);
    }

    public void start() {
        if (started) {
            LOGGER.warn("Publisher already started");
            return;
        }
        started = true;

        LOGGER.info("Binding to {}", address);
        publisher.bind(address);
    }

    public void stop() {
        LOGGER.info("Unbinding from {}", address);
        publisher.unbind(address);
        started = false;
    }
}
