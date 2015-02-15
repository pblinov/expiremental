import org.gridgain.grid.Grid;
import org.gridgain.grid.GridException;
import org.gridgain.grid.GridGain;
import org.gridgain.grid.lang.GridCallable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public class Example {
    private static final Logger LOGGER = Logger.getLogger(Example.class.toString());

    public static void main(String [] args) throws GridException {
        LOGGER.info("Starting...");

        try (Grid g = GridGain.start()) {
            Collection<GridCallable<Integer>> calls = new ArrayList<>();

            // Iterate through all the words in the sentence and create Callable jobs.
            for (final String word : "Count characters using callable".split(" ")) {
                calls.add(word::length);
            }

            // Execute collection of Callables on the grid.
            Collection<Integer> res = g.compute().call(calls).get();

            int sum = 0;

            // Add up individual word lengths received from remote nodes.
            for (int len : res) {
                sum += len;
            }

            LOGGER.info(">>> Total number of characters in the phrase is '" + sum + "'.");
        }

        LOGGER.info("Stop");
    }
}
