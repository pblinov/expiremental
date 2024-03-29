package algo;

import algo.order.OrderExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

import static algo.MarketData.BTC;
import static algo.MarketData.USD;
import static java.lang.String.format;

public class Strategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(Strategy.class);

    private static final double FEE = 0.2 / 100.0;
    private static final double LIMIT = 5.0 / 100.0;

    private final Balances balances;
    private final Portfolio portfolio;
    private final Converters converters;
    private double totalFee = 0.0;
    private final OrderExecutor orderExecutor;

    public Strategy(Balances balances, Portfolio portfolio, Converters converters, OrderExecutor orderExecutor) {
        this.balances = balances;
        this.portfolio = portfolio;
        this.converters = converters;
        this.orderExecutor = orderExecutor;
    }

    public void run() throws IOException {
        LOGGER.info("Start");

        double total = portfolio.currencies().stream().mapToDouble(this::normalizedQty).sum();
        LOGGER.info("[STATE] {}", format("Total balance: %.4f BTC, %.2f USD", total, toUsd(total)));

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
                    if (Math.abs(ratio) >= LIMIT) {
                        final Converter converter = converters.getConverter(currency, BTC);
                        final double baseQty = converter.round(balance.getCurrent() * ratio);
                        final double quoteQty = converter.convert(baseQty);
                        balance.add(baseQty);
                        btcBalance.add(-quoteQty);
                        final double fee = calculateFee(quoteQty);
                        totalFee += fee;
                        orderExecutor.executeOrder(baseQty, -quoteQty, currency, BTC, toUsd(expectedInBTC - currentInBTC), fee);
                    }
                });

        Stream.of(USD).forEach(currency -> {
            final double currentInBTC = round(normalizedQty(currency));
            final double expectedInBTC = round(total * portfolio.get(currency));
            final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
            final Balance balance = balances.getBalance(currency);
            balance.setExpected(balance.getCurrent() * (1 + ratio));
            if (Math.abs(ratio) >= LIMIT) {
                final Converter converter = converters.getConverter(BTC, USD);
                final double baseQty = converter.round(-converter.reverse().convert(balance.getCurrent() * ratio));
                final double quoteQty = converter.convert(baseQty);
                balance.add(-quoteQty);
                btcBalance.add(baseQty);
                final double fee = calculateFee(baseQty);
                totalFee += fee;
                orderExecutor.executeOrder(baseQty, -quoteQty, BTC, currency, toUsd(currentInBTC - expectedInBTC), fee);
            }
        });

        Stream.of(BTC).forEach(currency -> {
            final double currentInBTC = round(normalizedQty(currency));
            final double expectedInBTC = round(total * portfolio.get(currency));
            final double ratio = (expectedInBTC - currentInBTC) / currentInBTC;
            final Balance balance = balances.getBalance(currency);
            balance.setExpected(balance.getCurrent() * (1 + ratio));
        });

        LOGGER.info("[STATE] {}", format("Total fee: %.8f BTC %.4f USD", this.totalFee, toUsd(this.totalFee)));

        portfolio.currencies().stream()
                .sorted(String::compareTo)
                .forEach(currency -> LOGGER.info("[STATE] {}", balances.getBalance(currency)));

        LOGGER.info("Stop");
    }

    private double toUsd(double btcQty) {
        return converters.getConverter(BTC, USD).convert(btcQty);
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
        return converters.getConverter(currency, BTC).convert(balances.getBalance(currency).getCurrent());
    }
}
