package algo;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BalanceCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceCache.class);
    private final Map<String, Balance> balances = new HashMap<>();
    private final Wallet wallet;

    public BalanceCache(Wallet wallet) {
        this.wallet = wallet;
    }

    public Balance get(String currency) {
        return balances.computeIfAbsent(currency, this::request);
    }

    private Balance request(String currency) {
        return new Balance(currency, wallet.getBalance(new Currency(currency)).getAvailable().doubleValue());
    }
}
