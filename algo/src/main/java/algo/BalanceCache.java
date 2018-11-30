package algo;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BalanceCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceCache.class);
    private final Map<String, Balance> balances = new HashMap<>();
    private final Collection<Wallet> wallets;
    private final SymbolConverter symbolConverter;

    public BalanceCache(Collection<Wallet> wallets, SymbolConverter symbolConverter) {
        this.wallets = wallets;
        this.symbolConverter = symbolConverter;
    }

    public Balance get(String currency) {
        return balances.computeIfAbsent(currency, this::request);
    }

    private Balance request(String currency) {
        return new Balance(currency,
                wallets.stream()
                        .mapToDouble(w -> w.getBalance(new Currency(symbolConverter.encode(currency))).getAvailable().doubleValue())
                        .sum());
    }
}
