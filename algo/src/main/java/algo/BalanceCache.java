package algo;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.service.account.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BalanceCache implements Balances {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceCache.class);
    private final Map<String, Balance> balances = new HashMap<>();
    private Collection<Wallet> wallets;
    private final SymbolConverter symbolConverter;
    private final AccountService accountService;
    private final String exchange;

    public BalanceCache(String exchange, SymbolConverter symbolConverter, AccountService accountService) {
        this.exchange = exchange;
        this.symbolConverter = symbolConverter;
        this.accountService = accountService;
    }

    @Override
    public Balance getBalance(String currency) {
        return balances.computeIfAbsent(currency, this::request);
    }

    private void lazyInit() {
        if (wallets == null) {
            try {
                final AccountInfo accountInfo = accountService.getAccountInfo();
                wallets = accountInfo.getWallets().values();
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Cannot load %s wallets", exchange), e);
            }
        }
    }

    private Balance request(String currency) {
        lazyInit();
        return new Balance(currency,
                wallets.stream()
                        .mapToDouble(w -> w.getBalance(new Currency(symbolConverter.encode(currency))).getAvailable().doubleValue())
                        .sum());
    }
}
