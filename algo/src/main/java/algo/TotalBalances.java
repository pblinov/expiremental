package algo;

import java.util.Collection;

public class TotalBalances implements Balances {
    private final Collection<Balances> balances;

    public TotalBalances(Collection<Balances> balances) {
        this.balances = balances;
    }

    @Override
    public Balance getBalance(String currency) {
        final double totalCurrentValue = balances.stream()
                .map(balances -> balances.getBalance(currency))
                .mapToDouble(b -> b.getCurrent())
                .sum();
        return new Balance(currency, totalCurrentValue);
    }
}
