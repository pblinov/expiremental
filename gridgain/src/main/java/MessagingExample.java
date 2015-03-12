import org.gridgain.grid.*;
import org.gridgain.grid.cache.GridCacheConfiguration;
import org.gridgain.grid.cache.GridCacheDistributionMode;
import org.gridgain.grid.cache.GridCacheMode;
import org.gridgain.grid.cache.GridCacheWriteSynchronizationMode;

import java.util.concurrent.*;

/**
 * Example that demonstrates how to exchange messages between nodes. Use such
 * functionality for cases when you need to communicate to other nodes outside
 * of grid task.
 * <p>
 * To run this example you must have at least one remote node started.
 * <p>
 * Remote nodes should always be started with special configuration file which
 * enables P2P class loading: {@code 'ggstart.{sh|bat} examples/config/example-compute.xml'}.
 * <p>
 * Alternatively you can run {@link ComputeNodeStartup} in another JVM which will start GridGain node
 * with {@code examples/config/example-compute.xml} configuration.
 */
public final class MessagingExample {
    /** Number of messages. */
    private static final int MESSAGES_NUM = 100000;

    /**
     * Executes example.
     *
     * @param args Command line arguments, none required.
     * @throws GridException If example execution failed.
     */
    public static void main(String[] args) throws Exception {
        try (Grid g = GridGain.start(/*"examples/config/example-compute.xml"*/)) {
            if (g.nodes().size() < 2) {
                System.out.println();
                System.out.println(">>> Please start at least 2 grid nodes to run example.");
                System.out.println();

                return;
            }

            System.out.println();
            System.out.println(">>> Messaging example started.");

            //g.cache("aaa").dataStructures().

            // Projection for remote nodes.
            GridProjection rmtPrj = g.forRemotes();

            // Listen for messages from remote nodes to make sure that they received all the messages.
            int msgCnt = /*rmtPrj.nodes().size() **/ MESSAGES_NUM;

            CountDownLatch orderedLatch = new CountDownLatch(msgCnt);
            CountDownLatch unorderedLatch = new CountDownLatch(msgCnt);

            localListen(g.forLocal(), orderedLatch, unorderedLatch);

            final long startUnOrdered = System.currentTimeMillis();
            // Send unordered messages to all remote nodes.
            for (int i = 0; i < MESSAGES_NUM; i++) {
                rmtPrj.message().send("UNORDERED", Integer.toString(i));
            }

            System.out.println(">>> Finished sending unordered messages: " + (System.currentTimeMillis() - startUnOrdered));


            final long startOrdered = System.currentTimeMillis();
            // Send ordered messages to all remote nodes.
            for (int i = 0; i < MESSAGES_NUM; i++) {
                rmtPrj.message().sendOrdered("ORDERED", Integer.toString(i), 0);
            }

            System.out.println(">>> Finished sending ordered messages: " + (System.currentTimeMillis() - startOrdered));

            System.out.println(">>> Check output on all nodes for message printouts.");
            System.out.println(">>> Will wait for messages acknowledgements from all remote nodes.");

            orderedLatch.await();
            unorderedLatch.await();

            System.out.println(">>> Messaging example finished.");
        }
    }


    /**
     * Listen for messages from remote nodes.
     *
     * @param prj Grid projection.
     * @param orderedLatch Latch for ordered messages acks.
     * @param unorderedLatch Latch for unordered messages acks.
     */
    private static void localListen(
            GridProjection prj,
            final CountDownLatch orderedLatch,
            final CountDownLatch unorderedLatch
    ) {
        prj.message().localListen("ORDERED", (nodeId, msg) -> {
            //System.out.println(msg);
            orderedLatch.countDown();

            // Return true to continue listening, false to stop.
            return orderedLatch.getCount() > 0;
        });

        prj.message().localListen("UNORDERED", (nodeId, msg) -> {
            //System.out.println(msg);
            unorderedLatch.countDown();

            // Return true to continue listening, false to stop.
            return unorderedLatch.getCount() > 0;
        });
    }
}
