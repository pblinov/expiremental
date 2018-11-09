package algo;

public class Balance {
    private final String currency;
    private final double current;
    private double expected = 0.0;
    private double calculated = 0.0;

    public Balance(String currency, double current) {
        this.currency = currency;
        this.current = current;
        this.expected = current;
        this.calculated = current;
    }

    public double getCurrent() {
        return current;
    }

    public double getExpected() {
        return expected;
    }

    public void setExpected(double expected) {
        this.expected = expected;
    }

    public double getCalculated() {
        return calculated;
    }

    public void setCalculated(double calculated) {
        this.calculated = calculated;
    }

    public void add(double diff) {
        calculated += diff;
    }

    @Override
    public String toString() {
        final double ratio = (expected - current) / current * 100.0;
        final double ratioC = (expected - calculated) / calculated * 100.0;
        return String.format("%s [%4.1f|%4.1f] %.8f > %.8f (%.8f)", currency, ratio, ratioC, current, expected, calculated);
    }
}
