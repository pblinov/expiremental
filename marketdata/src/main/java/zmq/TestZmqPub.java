package zmq;

import marketdata.transport.Quote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.stream.IntStream;


/**
 * @author pblinov
 * @since 20/09/2017
 */
public class TestZmqPub {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmqPub.class);
    public static final String TOPIC_NAME = "A";
    public static final byte[] TOPIC = TOPIC_NAME.getBytes();
    public static final int HWM_SIZE = 10_000_000;

    public static void main(String[] args) throws InterruptedException {
        ZmqPublisherTransport transport = new ZmqPublisherTransport("tcp://*:7777", HWM_SIZE);
        transport.start();
        Thread.sleep(1_000);

        final long start = System.currentTimeMillis();
        IntStream.range(1, 5_000_000).forEach(i -> {
            transport.send(generateQuote(i));
        });
        LOGGER.info("Duration: {}", System.currentTimeMillis() - start);

        Thread.sleep(60_000);
    }

    public static Quote generateQuote(long size) {
        Quote quote = new Quote();
        quote.setSymbol("AAPL");
        quote.setExchange("NYSE");
        quote.setPrice(new BigDecimal("10.0"));
        quote.setSize(size);
        quote.setTimestamp(System.currentTimeMillis());
        return quote;
    }
}
