package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

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
        ZMQ.Context ctx = ZMQ.context(1);

        ZMQ.Socket publisher = ctx.socket(ZMQ.PUB);
        publisher.setHWM(HWM_SIZE);
        publisher.bind("tcp://*:7777");
        LOGGER.info("Bind");

        Thread.sleep(1_000);

        final long start = System.currentTimeMillis();
        IntStream.range(1, 5_000_000).mapToObj(Integer::toString).forEach(i -> {
            publisher.sendMore(TOPIC_NAME);
            publisher.send("" + System.currentTimeMillis());
            publisher.sendMore("B");
            publisher.send("" + System.currentTimeMillis());
//            try {
//                Thread.sleep(2L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        });
        LOGGER.info("Duration: {}", System.currentTimeMillis() - start);

        Thread.sleep(60_000);
    }
}
