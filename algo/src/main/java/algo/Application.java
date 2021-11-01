package algo;

import algo.exchange.BinanceMarketData;
import algo.exchange.ExmoMarketData;
import algo.exchange.HitbtcMarketData;
import algo.order.DummyExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static algo.MarketData.USD;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        final var binanceApiKey = args[0];
        final var binanceSecretKey = args[1];
        final var exmoApiKey = args[2];
        final var exmoSecretKey = args[3];
        final var hitbtcApiKey = args[4];
        final var hitbtcSecretKey = args[5];

        final Portfolio portfolio = new Portfolio();
        final TradeHistoryWriter tradeHistoryWriter = new TradeHistoryWriter();
        final BalanceWriter balanceWriter = new BalanceWriter();

        var binanceMarketData = new BinanceMarketData(binanceApiKey, binanceSecretKey, portfolio, tradeHistoryWriter, balanceWriter);
        final Collection<MarketData> exchanges = asList(
                binanceMarketData,
                new ExmoMarketData(exmoApiKey, exmoSecretKey, portfolio, tradeHistoryWriter, balanceWriter),
                new HitbtcMarketData(hitbtcApiKey, hitbtcSecretKey, portfolio, tradeHistoryWriter, balanceWriter)
        );

        final Balances balances = new AggregatedBalances(exchanges.stream()
                .map(MarketData::getBalances)
                .collect(Collectors.toList()));

        final Converters converters = new AggregatedConverters(exchanges.stream()
                .map(MarketData::getConverterService)
                .collect(Collectors.toList()));

        double totalPosition = exchanges.stream()
                .mapToDouble(exchange -> {
                    final TradeHistory tradeHistory = exchange.getTradeHistory();
                    final Collection<Position> positions = tradeHistory.positions();
                    LOGGER.debug("{}: {}", exchange.getName(), positions);
                    final double totalClosedPosition = positions.stream().mapToDouble(position -> converters.getConverter(position.getInstrument().getQuote(), USD).convert(position.getClosedPosition())).sum();
                    final double totalOpenPosition = positions.stream().mapToDouble(position -> converters.getConverter(position.getInstrument().getBase(), USD).convert(position.getOpenPosition())).sum();
                    LOGGER.info("{} total: {} USD", exchange.getName(), totalClosedPosition + totalOpenPosition);
                    return totalClosedPosition + totalOpenPosition;
                })
                .sum();

        LOGGER.info(format("Total position: %.2f USD", totalPosition));

        //var orderExecutor = new BinanceExecutor(binanceMarketData);
        var orderExecutor = new DummyExecutor();
        final Strategy strategy = new Strategy(balances, portfolio, converters, orderExecutor);
        strategy.run();

        tradeHistoryWriter.close();
        balanceWriter.close();
    }
}
