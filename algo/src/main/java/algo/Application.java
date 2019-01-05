package algo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws IOException {
        final Portfolio portfolio = new Portfolio();

        final Collection<MarketData> exchanges = asList(
                new BinanceMarketData(args[0], args[1], portfolio),
                new ExmoMarketData(args[2], args[3], portfolio),
                new HitbtcMarketData(args[4], args[5], portfolio)
        );

        final Balances balances = new AggregatedBalances(exchanges.stream()
                .map(MarketData::getBalances)
                .collect(Collectors.toList()));

        final Converters converters = new AggregatedConverters(exchanges.stream()
                .map(MarketData::getConverterService)
                .collect(Collectors.toList()));

        final Strategy strategy = new Strategy(balances, portfolio, converters);
        strategy.run();
    }
}
