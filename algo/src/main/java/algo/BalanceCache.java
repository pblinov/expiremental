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

import static algo.MarketData.BTC;
import static algo.MarketData.USD;
import static java.math.BigDecimal.ZERO;

public class BalanceCache implements Balances {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceCache.class);
    private final Map<String, Balance> balances = new HashMap<>();
    private Collection<Wallet> wallets;
    private final SymbolConverter symbolConverter;
    private final AccountService accountService;
    private final String exchange;
    private final BalanceWriter balanceWriter;
    private final Converters converterService;

    public BalanceCache(String exchange, SymbolConverter symbolConverter,
                        AccountService accountService,
                        BalanceWriter balanceWriter, Converters converterService) {
        this.exchange = exchange;
        this.symbolConverter = symbolConverter;
        this.accountService = accountService;
        this.balanceWriter = balanceWriter;
        this.converterService = converterService;
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
                wallets.forEach(wallet -> {
                    wallet.getBalances().forEach((currency, balance) -> {
                        if (balance.getAvailable().compareTo(ZERO) > 0) {
                            final String symbol = symbolConverter.decode(currency.getSymbol());
                            final double qty = balance.getAvailable().doubleValue();
                            balanceWriter.write(exchange, wallet.getName(), symbol,
                                    qty,
                                    convert(qty, symbol, BTC),
                                    convert(qty, symbol, USD)
                            );
                        }
                    });
                });
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Cannot load %s wallets", exchange), e);
            }
        }
    }

    private double convert(double qty, String from, String to) {
        try {
            return converterService.getConverter(from, to).convert(qty);
        } catch (Exception e) {
            LOGGER.warn("Cannot convert from {} to {}", from, to);
            return 0.0;
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
