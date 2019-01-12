package algo;

public class SimpleConverter implements Converter {
    private final String baseCurrency;
    private final String quoteCurrency;
    private final double qtyStep;
    private final double bid;
    private final double ask;

    public SimpleConverter(String baseCurrency, String quoteCurrency, double qtyStep, double bid, double ask) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.qtyStep = qtyStep;
        this.bid = bid;
        this.ask = ask;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public double getQtyStep() {
        return qtyStep;
    }

    @Override
    public double convert(double qty) {
        return qty * ask;
    }

    @Override
    public String toString() {
        return String.format("%s > %s %.8f %.8f/%.8f", baseCurrency, quoteCurrency, qtyStep, bid, ask);
    }

    @Override
    public SimpleConverter reverse() {
        return new SimpleConverter(quoteCurrency, baseCurrency, qtyStep, 1 / ask, 1 / bid);
    }

    @Override
    public double round(double expectedQty) {
        return Math.round(expectedQty / getQtyStep()) * getQtyStep();
    }
}
