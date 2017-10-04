package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import java.util.HashMap;
import java.util.Map;

import static zmq.TestZmqPub.HWM_SIZE;

/**
 * @author pblinov
 * @since 27/09/2017
 */
public class TestZmqProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmqProxy.class);
    public static final byte[] ALL = "".getBytes();

    public static void main(String[] args) {
        ZMQ.Context ctx = ZMQ.context(1);

        ZMQ.Socket publisher = ctx.socket(ZMQ.XPUB);
        publisher.setHWM(HWM_SIZE);
        publisher.bind("tcp://*:8888");
        publisher.setXpubVerbose(true);
        LOGGER.info("Bind");

        ZMQ.Socket subscriber = ctx.socket(ZMQ.SUB);
        subscriber.setHWM(HWM_SIZE);
        subscriber.connect("tcp://localhost:7777");
        LOGGER.info("Connect");

        subscriber.subscribe(ALL);

        Map<String, byte[]> cache = new HashMap<>();

        ZMQ.Poller poller = ctx.poller();
        int pubIdx = poller.register(publisher, ZMQ.Poller.POLLIN);
        int subIdx = poller.register(subscriber, ZMQ.Poller.POLLIN);

        while (true) {
            if (poller.poll(5) == -1) {
                LOGGER.error("Cannot poll");
                return;
            }

            ZMQ.PollItem subItem = poller.getItem(subIdx);
            if (subItem.isReadable()) {
                String topic = subscriber.recvStr();
                byte[] message = subscriber.recv();
                if (topic == null) {
                    LOGGER.error("Topic is unknown");
                    return;
                }
                cache.put(topic, message);
                publisher.sendMore(topic);
                publisher.send(message);
            }


            ZMQ.PollItem pubItem = poller.getItem(pubIdx);
            if (pubItem.isReadable()) {
                ZFrame frame = ZFrame.recvFrame(publisher);

                if (frame == null) {
                    continue;
                }
                //  Event is one byte 0=unsub or 1=sub, followed by topic
                byte[] event = frame.getData();
                if (event [0] == 1) {
                    String topic = new String(event, 1, event.length -1);
                    LOGGER.info("Subscribe: {}", topic);
                    if (topic.isEmpty()) {
                        cache.forEach((t, m) -> {
                            publisher.sendMore(t);
                            publisher.send(m);
                        });
                    } else {
                        byte[] m = cache.get(topic);
                        if (m != null) {
                            publisher.sendMore(topic);
                            publisher.send(m);
                        }
                    }
                } else if (event [0] == 0) {
                    String topic = new String(event, 1, event.length -1);
                    LOGGER.info("Unsubscribe: {}", topic);
                }
                frame.destroy();
            }
        }
    }
}
