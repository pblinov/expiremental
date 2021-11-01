package algo.order;

public interface OrderExecutor {
    void executeOrder(double baseQty,
                      double quoteQty,
                      String currency,
                      String toCurrency,
                      double deltaInUsd,
                      double expectedFee);
}
