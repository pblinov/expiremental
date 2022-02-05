package algo;

import java.util.Collection;
import java.util.List;

public class SimplePortfolio implements Portfolio {
    private final List<String> currencies;

    public SimplePortfolio(List<String> currencies) {
        this.currencies = currencies;
    }

    @Override
    public double get(String currency) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> currencies() {
        return currencies;
    }
}
