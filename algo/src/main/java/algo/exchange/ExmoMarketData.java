package algo.exchange;

import algo.MarketData;
import algo.Portfolio;
import algo.SymbolConverter;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exmo.ExmoExchange;
import org.knowm.xchange.exmo.dto.trade.ExmoTradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ExmoMarketData extends MarketData implements SymbolConverter {
    public ExmoMarketData(String apiKey, String secretKey, Portfolio portfolio) throws IOException {
        super(apiKey, secretKey, portfolio);
    }

    @Override
    protected Collection<TradeHistoryParams> getTradeHistoryParams() {
        return Arrays.asList(
                new ExmoTradeHistoryParams(10, 0L, pairs(BTC)),
                new ExmoTradeHistoryParams(10, 0L, pairs(USD)),
                new ExmoTradeHistoryParams(10, 0L, pairs(ETH)),
                new ExmoTradeHistoryParams(10, 0L, pairs(RUB))
        );
    }

    @Override
    protected ExchangeSpecification createSpecification(String apiKey, String secretKey) {
        ExchangeSpecification specification = new ExchangeSpecification(ExmoExchange.class);
        specification.setApiKey(apiKey);
        specification.setSecretKey(secretKey);
        specification.setHost("api.exmo.me");
        specification.setSslUri("https://api.exmo.me");
        return specification;
    }

    @Override
    protected SymbolConverter getSymbolConverter() {
        return this;
    }

    @Override
    protected String getName() {
        return "EXMO";
    }

    @Override
    public String encode(String currency) {
        return currency;
    }

    @Override
    public String decode(String currency) {
        return currency;
    }
}
