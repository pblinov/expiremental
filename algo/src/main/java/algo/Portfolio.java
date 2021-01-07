package algo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static algo.MarketData.*;

public class Portfolio {
    private final Map<String, Double> expectedLevels = new HashMap<>();

    public Portfolio() {
        final double usdLevel = 0.2;
        expectedLevels.put(USD, usdLevel);

        final double ethLevel = 0.1;
        expectedLevels.put(ETH, ethLevel);

        final double btcLevel = 0.1;
        expectedLevels.put(BTC, btcLevel);

        final double rubLevel = 0.0;
        expectedLevels.put(RUB, rubLevel);

        final String [] other = new String[] {"BCH", "BNB", "EOS", "LTC", "NEO", "XMR", "TRX", "ADA", "XLM", "XRP"};

        final double level = (1 - usdLevel - ethLevel - btcLevel - rubLevel) / other.length;
        Stream.of(other).forEach(currency -> expectedLevels.put(currency, level));
    }

    public double get(String currency) {
        return expectedLevels.get(currency);
    }

    public Collection<String> currencies() {
        return expectedLevels.keySet();
    }
}
