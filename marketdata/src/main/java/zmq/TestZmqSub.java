package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
        subscriber.connect("tcp://localhost:8888");
        LOGGER.info("Connect");

        subscriber.subscribe(TOPIC);

        int counter = 0;
        while (true) {
            String topic = subscriber.recvStr();
            String message = subscriber.recvStr();
            counter++;
            if (counter % 100000 == 0) {
            //executor.execute(() -> {
                try {
                    LOGGER.info("{} {} {}", topic, message, System.currentTimeMillis() - Long.parseLong(message.split("\\|")[1]));
                } catch (Exception e) {
                    LOGGER.error("{} {}", topic, message);
                }
            //});
            }
            //LOGGER.info("{} {}", topic, message);
        }
    }
}
