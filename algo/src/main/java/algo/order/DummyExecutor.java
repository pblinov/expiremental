package algo.order;

import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;

@Slf4j
public class DummyExecutor implements OrderExecutor {
    @Override
    public void executeOrder(final double baseQty,
                             final double quoteQty,
                             final String currency,
                             final String toCurrency,
                             final double deltaInUsd,
                             final double expectedFee) {
        log.info("[ACTION] {}", format("%.8f %s > %.8f %s (diff: $%.1f, fee: %.8f)", baseQty, currency, quoteQty, toCurrency, deltaInUsd, expectedFee));
    }
}
