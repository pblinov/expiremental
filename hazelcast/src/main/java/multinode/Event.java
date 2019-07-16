package multinode;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.core.PartitionAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static multinode.TestMultiNodeExecution.COUNTER;

public class Event implements Runnable, Serializable, HazelcastInstanceAware, PartitionAware<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Event.class);

    private static final Map<String, Integer> map = new ConcurrentHashMap<>();

    private final int id;
    private final String group;
    private transient HazelcastInstance hz;

    public Event(int id, String group) {
        this.id = id;
        this.group = group;
    }

    @Override
    public void run() {
        if (!hz.getLifecycleService().isRunning()) {
            //LOGGER.warning("Stopped " + hz.getName());
            return;
        }

        //final IMap<String, Integer> map = hz.getMap("test");
        final Integer lastValue = map.get(getGroup());
        if (lastValue == null) {
            // First event
            LOGGER.info("FIRST " + toString());
            map.put(getGroup(), getId());
        } else if (lastValue + 1 < getId()) {
            // Lost event detected
            LOGGER.error("LOST " + toString() + " but expected " + (lastValue + 1));
            map.put(getGroup(), getId());
        } else if (lastValue + 1 > getId()) {
            LOGGER.warn("SKIP " + toString() + " but expected " + (lastValue + 1));
        } else {
            // Normal element
            map.put(getGroup(), getId());
            LOGGER.info("OK " + toString());
        }

        if (getId() == COUNTER - 1) {
            LOGGER.info("LAST " + toString());
        } else if (getId() % (COUNTER / 10) == 0 && getId() != 0) {
            LOGGER.info("MID " + toString());
        }
    }

    @Override
    public String toString() {
        return String.format("E{%d %s} %s", id, group, hz.getName());
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
    }

    @Override
    public String getPartitionKey() {
        return getGroup();
    }

    public int getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }
}
