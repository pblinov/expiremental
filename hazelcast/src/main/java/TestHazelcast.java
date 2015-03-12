import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * @author pblinov
 * @since 12/03/2015
 */
public class TestHazelcast {
    private static final Logger LOGGER = Logger.getLogger(TestHazelcast.class.getName());

    public static void main(String [] args) {
        LOGGER.info(String.format("JVM: %s", System.getProperty("java.version")));


        MapConfig mapConfig = new MapConfig();
        mapConfig.setName( "customers" );
        mapConfig.setBackupCount( 0 );
        mapConfig.setAsyncBackupCount( 1 );
        mapConfig.getMaxSizeConfig().setSize( 200000 );
        mapConfig.setOptimizeQueries(true);
        //mapConfig.setTimeToLiveSeconds( 300 );

        Config config = new Config();
        config.setInstanceName("my-instance");
        config.addMapConfig( mapConfig );

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        Map<Integer, String> customers = hazelcastInstance.getMap( "customers" );
        customers.put( 1, "Joe" );
        customers.put( 2, "Ali" );
        customers.put( 3, "Avi" );
        final long startCustomers = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            customers.put( i, String.format("Customer #%d", i));
        }
        LOGGER.info("Stop customers: " + (System.currentTimeMillis() - startCustomers) + " / " + customers.size());

        LOGGER.info("Customer with key 1: " + customers.get(1));
        //System.out.println( "Map Size:" + hazelcastInstance.size() );
        customers.clear();


        Queue<String> queueCustomers = hazelcastInstance.getQueue( "customers" );
        queueCustomers.offer( "Tom" );
        queueCustomers.offer( "Mary" );
        queueCustomers.offer( "Jane" );
        LOGGER.info("First customer: " + queueCustomers.poll());
        LOGGER.info("Second customer: " + queueCustomers.peek());
        LOGGER.info("Queue size: " + queueCustomers.size());

        final long startSending = System.currentTimeMillis();
        LOGGER.info("Start sending");
        for (int i = 0; i < 100000; i++) {
            queueCustomers.offer(String.format("Customer #%d", i));
        }
        LOGGER.info("Stop sending: " + (System.currentTimeMillis() - startSending) + " / " + queueCustomers.size());

        final long startPolling = System.currentTimeMillis();
        LOGGER.info("Start polling");
        for (int i = 0; i < 100000; i++) {
            queueCustomers.poll();
        }
        LOGGER.info("Stop polling: " + (System.currentTimeMillis() - startPolling) + " / " + queueCustomers.size());
    }
}
