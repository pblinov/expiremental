package algo.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

public class DummyExecutor implements OrderExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyExecutor.class);

    @Override
    public void executeOrder(final double baseQty,
                             final double quoteQty,
                             final String currency,
                             final String toCurrency,
                             final double deltaInUsd,
                             final double expectedFee) {
        LOGGER.info("[ACTION] {}", format("%.8f %s > %.8f %s (diff: $%.1f, fee: %.8f)", baseQty, currency, quoteQty, toCurrency, deltaInUsd, expectedFee));
    }
}
