package zmq;

import intrino.SecurityParser;
import marketdata.QuoteSerializer;
import marketdata.transport.Quote;
import marketdata.transport.QuoteListener;
import marketdata.transport.SubscriberTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static zmq.TestZmqPub.HWM_SIZE;
import static zmq.TestZmqPub.TOPIC;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class ZmqSubscriberTransport implements SubscriberTransport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZmqSubscriberTransport.class);

    private QuoteListener listener;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean started;
    private String address;
    private ZMQ.Socket subscriber;
    private QuoteSerializer quoteSerializer = new QuoteSerializer();

    public ZmqSubscriberTransport(String address, int hwmSize) {
        this.address = address;
        ZMQ.Context ctx = ZMQ.context(1);
        subscriber = ctx.socket(ZMQ.SUB);
        subscriber.setHWM(hwmSize);
    }

    @Override
    public void addListener(QuoteListener listener) {
        this.listener = listener;
    }

    @Override
    public void subscribe(String symbol) {
        subscriber.subscribe(symbol.getBytes());
    }

    @Override
    public void unsubscribe(String symbol) {
        subscriber.unsubscribe(symbol.getBytes());
    }

    public void start() {
        if (started) {
            LOGGER.warn("Subscriber already started");
            return;
        }

        started = true;

        LOGGER.info("Connecting to {}", address);
        subscriber.connect(address);
        executorService.execute(() -> {
            while (started) {
                String topic = subscriber.recvStr();
                byte[] message = subscriber.recv();
                if (listener != null) {
                    try {
                        listener.onQuote(quoteSerializer.deserialize(message));
                    } catch (Exception e) {
                        LOGGER.error("Cannot deserialize", e);
                    }
                }
            }
        });
    }

    public void stop() {
        LOGGER.info("Disonnecting from {}", address);
        started = false;

    }
}
