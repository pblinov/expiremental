package zmq;

import marketdata.transport.QuoteListener;
import marketdata.transport.SubscriberTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import static zmq.TestZmqPub.HWM_SIZE;
import static zmq.TestZmqPub.TOPIC;
import static zmq.TestZmqPub.TOPIC_NAME;

/**
 * @author pblinov
 * @since 27/09/2017
 */
public class TestZmqSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmqSub.class);

    static long counter = 0;

    public static void main(String[] args) throws InterruptedException {
        ZmqSubscriberTransport transport = new ZmqSubscriberTransport("tcp://localhost:7777", HWM_SIZE);
        transport.subscribe("AAPL");

        final long start = System.currentTimeMillis();

        transport.addListener(getQuoteListener(start));
        transport.start();

        Thread.sleep(60_000);
    }

    public static QuoteListener getQuoteListener(long start) {
        return (quote) -> {
            counter++;
            if (counter % 100_000 == 0) {
                long now = System.currentTimeMillis();
                LOGGER.info("#{} Latency : {}, Throughput: {}", quote.getSize(), now - quote.getTimestamp(), counter / ((now - start) / 1000d));
            }
        };
    }
}
