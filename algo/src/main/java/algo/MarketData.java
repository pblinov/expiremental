package algo;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

public class MarketData {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketData.class);
    private static final String BTC = "BTC";
    private static final String USDT = "USDT";
    private static final double FEE = 0.075 / 100;
    private static final double LIMIT = 0.04;

    private final BalanceCache balances;
    private final Portfolio portfolio;
    private final ConverterService converterService;
    private double totalFee = 0.0;

    public static void main(String[] args) throws IOException {
        MarketData application = new MarketData(args[0], args[1]);
        application.run();
    }

    public MarketData(String apiKey, String secretKey) throws IOException {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class.getName(),apiKey, secretKey);
        MarketDataService marketDataService = exchange.getMarketDataService();
        AccountService accountService = exchange.getAccountService();
        AccountInfo accountInfo = accountService.getAccountInfo();
        Wallet wallet = accountInfo.getWallet();
        ExchangeMetaData metaData = exchange.getExchangeMetaData();
        balances = new BalanceCache(wallet);
        portfolio = new Portfolio();
        converterService = new ConverterService(marketDataService, metaData, portfolio.currencies());
    }

    private void run() throws IOException {
        LOGGER.info("Start");

        double total = portfolio.currencies().stream().mapToDouble(this::normalizedQty).sum();
        LOGGER.info("Total: {}", total);

        final Balance btcBalance = balances.get(BTC);

        portfolio.currencies().stream()
                .filter(c -> !c.equals(USDT))
                .filter(c -> !c.equals(BTC))
                .forEach(currency -> {
                    final double currentInBTC = round(normalizedQty(currency));
                    final double expectedInBTC = round(total * portfolio.get(currency));
                    final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
                    final Balance balance = balances.get(currency);
                    balance.setExpected(balance.getCurrent() * (1 + ratio));
                    if (Math.abs(ratio) > LIMIT) {
                        final Converter converter = converterService.get(currency, BTC);
                        final double baseQty = converter.round(balance.getCurrent() * ratio);
                        final double quoteQty = converter.convert(baseQty);
                        balance.add(baseQty);
                        btcBalance.add(-quoteQty);
                        final double fee = calculateFee(quoteQty);
                        totalFee += fee;
                        LOGGER.info("{}", String.format("%.8f %s > %.8f %s (fee: %.8f)", baseQty, currency, -quoteQty, BTC, fee));
                    }
                });

        Stream.of(USDT).forEach(currency -> {
            final double currentInBTC = round(normalizedQty(currency));
            final double expectedInBTC = round(total * portfolio.get(currency));
            final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
            final Balance balance = balances.get(currency);
            balance.setExpected(balance.getCurrent() * (1 + ratio));
            if (Math.abs(ratio) > LIMIT) {
                final Converter converter = converterService.get(BTC, USDT);
                final double baseQty = converter.round(-converter.reverse().convert(balance.getCurrent() * ratio));
                final double quoteQty = converter.convert(baseQty);
                balance.add(-quoteQty);
                btcBalance.add(baseQty);
                final double fee = calculateFee(baseQty);
                totalFee += fee;
                LOGGER.info("{}", String.format("%.8f %s > %.8f %s (fee: %.8f)", baseQty, BTC, -quoteQty, currency, fee));
            }
        });

        portfolio.currencies().stream()
                .sorted(String::compareTo)
                .forEach(currency -> LOGGER.info("{}", balances.get(currency)));

        LOGGER.info("{}", String.format("Total fee: %.8f BTC %.4f USD", totalFee, converterService.get(BTC, USDT).convert(totalFee)));

        LOGGER.info("Stop");
    }

    private static double calculateFee(double qty) {
        return Math.abs(qty * FEE);
    }

    private static double round(double value) {
        return round(value, 10_000.0);
    }

    private static double round(double value, double precision) {
        return Math.round(value * precision) / precision;
    }

    private double normalizedQty(String currency) {
        return converterService.get(currency, BTC).convert(balances.get(currency).getCurrent());
    }

}
