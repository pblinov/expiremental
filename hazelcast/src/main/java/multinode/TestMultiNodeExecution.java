package multinode;

import com.hazelcast.config.Config;
import com.hazelcast.config.DurableExecutorConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TestMultiNodeExecution {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestMultiNodeExecution.class);
    public static final String EXECUTOR = "test";
    public static final int COUNTER = 100_000;

    public static void main(String[] args) {
        final HazelcastInstance[] instance = new HazelcastInstance[3];
        for (int i = 0; i < instance.length; i++) {
            instance[i] = createInstance();
        }

        final ExecutorService executor = Executors.newFixedThreadPool(10);
        executor.execute(() -> send("A", instance[0]));
        executor.execute(() -> send("B", instance[0]));
        executor.execute(() -> send("C", instance[0]));
        executor.execute(() -> send("D", instance[0]));
        executor.execute(() -> send("E", instance[0]));
        executor.execute(() -> send("F", instance[0]));

        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.schedule(() -> {
            LOGGER.info("Shutdown in progress {}", instance[1].getName());
            instance[1].shutdown();
            LOGGER.info("Shutdown done {}", instance[1].getName());
        }, 10, TimeUnit.SECONDS);
        scheduledExecutor.schedule(() -> createInstance(), 20, TimeUnit.SECONDS);
        scheduledExecutor.schedule(() -> createInstance(), 30, TimeUnit.SECONDS);
    }

    private static void send(String group, HazelcastInstance instance) {
        IntStream.range(0, COUNTER).forEach(i -> send(i, group, instance));
    }

    private static void send(int id, String group, HazelcastInstance instance) {
        final Event event = new Event(id, group);
        instance.getDurableExecutorService(EXECUTOR).executeOnKeyOwner(event, event);
    }

    private static HazelcastInstance createInstance() {
        final Config config = new Config();
        final DurableExecutorConfig executorConfig = new DurableExecutorConfig();
        executorConfig.setName(EXECUTOR);
        executorConfig.setCapacity(COUNTER * 10);
        executorConfig.setPoolSize(1);
        config.addDurableExecutorConfig(executorConfig);
        LOGGER.info("Start in progress");
        final HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        LOGGER.info("Started {}", instance.getName());
        return instance;
    }
}
