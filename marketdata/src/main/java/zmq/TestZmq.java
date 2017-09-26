package zmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


/**
 * @author pblinov
 * @since 20/09/2017
 */
public class TestZmq {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestZmq.class);
    public static final String TOPIC_NAME = "A";
    private static final byte[] TOPIC = TOPIC_NAME.getBytes();

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws InterruptedException {
        ZMQ.Context ctx = ZMQ.context(1);

        ZMQ.Socket publisher = ctx.socket(ZMQ.XPUB);
        publisher.bind("tcp://*:7777");
        publisher.setXpubVerbose(true);
        LOGGER.info("Bind");

        ZMQ.Socket subscriber1 = ctx.socket(ZMQ.SUB);
        subscriber1.connect("tcp://localhost:7777");
        LOGGER.info("Connect");

        ZMQ.Socket subscriber2 = ctx.socket(ZMQ.SUB);
        subscriber2.connect("tcp://localhost:7777");
        LOGGER.info("Connect");


        subscriber1.subscribe(TOPIC);
        subscriber2.subscribe(TOPIC);

        Thread.sleep(1000);

        executor.execute(() -> {
            while (true) {
                ZMQ.Poller poller = ctx.poller();
                int idx = poller.register(publisher, ZMQ.Poller.POLLIN);

                if (poller.poll(1000) == -1)
                    continue;              //  Interrupted

                //  Any new topic data we cache and then forward
                ZMQ.PollItem item = poller.getItem(idx);
                if (item.isReadable()) {
                    ZFrame frame = ZFrame.recvFrame(publisher);

                    if (frame == null)
                        continue;
                    //  Event is one byte 0=unsub or 1=sub, followed by topic
                    byte[] event = frame.getData();
                    if (event [0] == 1) {
                        String topic = new String(event, 1, event.length -1);
                        System.out.printf ("Sending cached topic %s\n", topic);
                    }
                    frame.destroy();
                }
            }
        });

        executor.execute(() -> {
            IntStream.range(1, 5).mapToObj(Integer::toString).forEach(i -> {
                publisher.sendMore(TOPIC_NAME);
                publisher.send(i);
            });
        });


        executor.execute(() -> {
            while (true) {
                String topic = subscriber1.recvStr();
                String message = subscriber1.recvStr();

                subscriber1.subscribe("C".getBytes());

                LOGGER.info("{}: {}", 1, message);
            }
        });


        executor.execute(() -> {
            while (true) {
                String topic = subscriber2.recvStr();
                String message = subscriber2.recvStr();

                subscriber2.subscribe("D".getBytes());

                LOGGER.info("{}: {}", 2, message);
            }
        });
    }
}
