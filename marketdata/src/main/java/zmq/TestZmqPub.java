package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


/**
 * @author pblinov
 * @since 20/09/2017
 */
public class TestZmqPub {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmqPub.class);
    public static final String TOPIC_NAME = "A";
    public static final byte[] TOPIC = TOPIC_NAME.getBytes();

    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context ctx = ZMQ.context(1);

        ZMQ.Socket publisher = ctx.socket(ZMQ.PUB);
        publisher.bind("tcp://*:7777");
        LOGGER.info("Bind");

        Thread.sleep(1000);

        IntStream.range(1, 5000000).mapToObj(Integer::toString).forEach(i -> {
            publisher.sendMore(TOPIC_NAME);
            publisher.send(i + "AAA|" + System.currentTimeMillis());
            publisher.sendMore("B");
            publisher.send(i + "BBB|" + System.currentTimeMillis());
        });
    }
}
