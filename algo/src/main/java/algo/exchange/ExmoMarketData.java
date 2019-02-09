package algo.exchange;

import algo.*;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exmo.ExmoExchange;
import org.knowm.xchange.exmo.dto.trade.ExmoTradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

public class ExmoMarketData extends MarketData implements SymbolConverter {
    public ExmoMarketData(String apiKey, String secretKey, Portfolio portfolio, TradeHistoryWriter tradeHistoryWriter, BalanceWriter balanceWriter) throws IOException {
        super(apiKey, secretKey, portfolio, tradeHistoryWriter, balanceWriter);
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
        switch (currency) {
            case "USDF":
                return "USD";
            case "USD":
                return "USD";
            default:
            return currency;
        }
    }

    @Override
    public String decode(String currency) {
        switch (currency) {
            case "USD":
                return "USD";
            case "USDT":
                return "USD";
            case "руб.":
                return "RUB";
            default:
                return currency;
        }
    }

    protected Date getPositionThreshold() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }
}
