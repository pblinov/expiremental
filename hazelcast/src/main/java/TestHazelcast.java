import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.Predicates;

import java.util.Map;
import java.util.logging.Logger;

public class TestHazelcast {
    private static final Logger LOGGER = Logger.getLogger(TestHazelcast.class.getName());

    public static void main(String [] args) {
        HazelcastInstance hz = Hazelcast.newHazelcastInstance();
        IMap<Integer, Item> map = hz.getMap("TEST");
        map.addIndex("name", false);

        LOGGER.info("Start");
        for (int i = 0; i < 2_000_000; i++) {
            Item item = new Item();
            item.setId(i);
            item.setName(String.format("A%s", i % 400));
            map.put(i, item);
        }
        LOGGER.info("Items added");
        LOGGER.info("Count: " + map.values(Predicates.equal("name", "A1")).size());
        LOGGER.info("Count: " + map.values(Predicates.equal("name", "A100")).size());
        LOGGER.info("Count: " + map.values(Predicates.equal("name", "A50")).size());
        LOGGER.info("Stop");
    }
}
