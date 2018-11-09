package algo;

public class Converter {
    private final String baseCurrency;
    private final String quoteCurrency;
    private final double qtyStep;
    private final double bid;
    private final double ask;

    public Converter(String baseCurrency, String quoteCurrency, double qtyStep, double bid, double ask) {
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

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double convert(double qty) {
        return qty * ask;
    }

    @Override
    public String toString() {
        return String.format("%s > %s %.8f %.8f/%.8f", baseCurrency, quoteCurrency, qtyStep, bid, ask);
    }

    public Converter reverse() {
        return new Converter(quoteCurrency, baseCurrency, qtyStep, 1 / ask, 1 / bid);
    }

    public double round(double expectedQty) {
        return Math.round(expectedQty / getQtyStep()) * getQtyStep();
    }
}
