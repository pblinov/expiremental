package algo;

import algo.exchange.BinanceMarketData;
import algo.exchange.ExmoMarketData;
import algo.exchange.HitbtcMarketData;
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
        final Portfolio portfolio = new Portfolio();
        final TradeHistoryWriter tradeHistoryWriter = new TradeHistoryWriter();
        final BalanceWriter balanceWriter = new BalanceWriter();

        final Collection<MarketData> exchanges = asList(
                new BinanceMarketData(args[0], args[1], portfolio, tradeHistoryWriter, balanceWriter),
                new ExmoMarketData(args[2], args[3], portfolio, tradeHistoryWriter, balanceWriter),
                new HitbtcMarketData(args[4], args[5], portfolio, tradeHistoryWriter, balanceWriter)
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

        final Strategy strategy = new Strategy(balances, portfolio, converters);
        strategy.run();

        tradeHistoryWriter.close();
        balanceWriter.close();
    }
}
