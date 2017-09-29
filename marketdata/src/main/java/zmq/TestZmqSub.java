package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import static zmq.TestZmqPub.HWM_SIZE;
import static zmq.TestZmqPub.TOPIC;

/**
 * @author pblinov
 * @since 27/09/2017
 */
public class TestZmqSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmqSub.class);

    //private static Executor executor = Executors.newFixedThreadPool(1);

    public static void main(String[] args) {
        ZMQ.Context ctx = ZMQ.context(1);
        ZMQ.Socket subscriber = ctx.socket(ZMQ.SUB);
        subscriber.setHWM(HWM_SIZE);
        subscriber.connect("tcp://localhost:8888");
        LOGGER.info("Connect");

        subscriber.subscribe(TOPIC);

        int counter = 0;
        while (true) {
            String topic = subscriber.recvStr();
            String message = subscriber.recvStr();
            if (counter % 100_000 == 0) {
                try {
                    LOGGER.info("Latency {} {}: {}", topic, message, System.currentTimeMillis() - Long.parseLong(message));
                } catch (NumberFormatException e) {
                    LOGGER.error("Cannot parse: {} {}", topic, message);
                }
            }
            counter++;
        }
    }
}
