package algo;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

public abstract class MarketData {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketData.class);
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USD = "USD";
    private static final double FEE = 0.075 / 100;
    private static final double LIMIT = 0.04;

    private final Balances balances;
    private final Portfolio portfolio;
    private final ConverterServiceInterface converterService;
    private double totalFee = 0.0;

    public MarketData(String apiKey, String secretKey, Portfolio portfolio) throws IOException {
        this.portfolio = portfolio;
        Exchange exchange = createExchange(apiKey, secretKey);
        balances = new BalanceCache(getName(), getSymbolConverter(), exchange.getAccountService());

        MarketDataService marketDataService = exchange.getMarketDataService();
        ExchangeMetaData metaData = exchange.getExchangeMetaData();
        converterService = new ConverterService(getName(), marketDataService, metaData, portfolio.currencies(), getSymbolConverter());
    }

    static boolean isMain(String currency) {
        return currency.equals(BTC) || currency.equals(USD);
    }

    private Exchange createExchange(String apiKey, String secretKey) {
        ExchangeSpecification specification = createSpecification(apiKey, secretKey);
        return ExchangeFactory.INSTANCE.createExchange(specification);
    }

    protected abstract ExchangeSpecification createSpecification(String apiKey, String secretKey);
    protected abstract SymbolConverter getSymbolConverter();
    protected abstract String getName();

    public void run() throws IOException {
        LOGGER.info("{} Start", getName());

        double total = portfolio.currencies().stream().mapToDouble(this::normalizedQty).sum();
        LOGGER.info("{} Total: {} BTC, {} USD", getName(), total, toUsd(total));

        final Balance btcBalance = balances.getBalance(BTC);

        portfolio.currencies().stream()
                .filter(c -> !c.equals(USD))
                .filter(c -> !c.equals(BTC))
                .forEach(currency -> {
                    final double currentInBTC = round(normalizedQty(currency));
                    final double expectedInBTC = round(total * portfolio.get(currency));
                    final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
                    final Balance balance = balances.getBalance(currency);
                    balance.setExpected(balance.getCurrent() * (1 + ratio));
                    if (Math.abs(ratio) > LIMIT) {
                        final Converter converter = converterService.getConverter(currency, BTC);
                        final double baseQty = converter.round(balance.getCurrent() * ratio);
                        final double quoteQty = converter.convert(baseQty);
                        balance.add(baseQty);
                        btcBalance.add(-quoteQty);
                        final double fee = calculateFee(quoteQty);
                        totalFee += fee;
                        LOGGER.info("{}", String.format("%.8f %s > %.8f %s (fee: %.8f)", baseQty, currency, -quoteQty, BTC, fee));
                    }
                });

        Stream.of(USD).forEach(currency -> {
            final double currentInBTC = round(normalizedQty(currency));
            final double expectedInBTC = round(total * portfolio.get(currency));
            final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
            final Balance balance = balances.getBalance(currency);
            balance.setExpected(balance.getCurrent() * (1 + ratio));
            if (Math.abs(ratio) > LIMIT) {
                final Converter converter = converterService.getConverter(BTC, USD);
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
                .forEach(currency -> LOGGER.info("{}", balances.getBalance(currency)));

        LOGGER.info("{} {}", getName(), String.format("Total fee: %.8f BTC %.4f USD", this.totalFee, toUsd(this.totalFee)));

        LOGGER.info("{} Stop", getName());
    }

    private double toUsd(double btcQty) {
        return converterService.getConverter(BTC, USD).convert(btcQty);
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
        return converterService.getConverter(currency, BTC).convert(balances.getBalance(currency).getCurrent());
    }

    public Balances getBalances() {
        return balances;
    }

    public ConverterServiceInterface getConverterService() {
        return converterService;
    }
}
