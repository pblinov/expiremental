package algo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AggregatedBalances implements Balances {
    private final Collection<Balances> balances;
    private final Map<String, Balance> cache = new HashMap<>();

    public AggregatedBalances(Collection<Balances> balances) {
        this.balances = balances;
    }

    @Override
    public Balance getBalance(String currency) {
        return cache.computeIfAbsent(currency, this::calculateBalance);
    }

    private Balance calculateBalance(String currency) {
        final double totalCurrentValue = balances.stream()
                .map(balances -> balances.getBalance(currency))
                .mapToDouble(b -> b.getCurrent())
                .sum();
        return new Balance(currency, totalCurrentValue);
    }
}
