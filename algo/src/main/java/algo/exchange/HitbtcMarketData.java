package algo.exchange;

import algo.*;
import algo.history.TradeHistoryWriter;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.hitbtc.v2.HitbtcExchange;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

public class HitbtcMarketData extends MarketData implements SymbolConverter {
    public HitbtcMarketData(String apiKey, String secretKey, Portfolio portfolio, TradeHistoryWriter tradeHistoryWriter, BalanceWriter balanceWriter) throws IOException {
        super(apiKey, secretKey, portfolio, tradeHistoryWriter, balanceWriter);
    }

    @Override
    protected Collection<TradeHistoryParams> getTradeHistoryParams() {
        return Collections.singleton(exchange.getTradeService().createTradeHistoryParams());
    }

    @Override
    protected ExchangeSpecification createSpecification(String apiKey, String secretKey) {
        ExchangeSpecification specification = new ExchangeSpecification(HitbtcExchange.class);
        specification.setApiKey(apiKey);
        specification.setSecretKey(secretKey);
        return specification;
    }

    @Override
    protected SymbolConverter getSymbolConverter() {
        return this;
    }

    @Override
    protected String getName() {
        return "HitBTC";
    }

    @Override
    public String encode(String currency) {
        switch (currency) {
            case "USD":
                return "USD";
            default:
                return currency;
        }
    }

    @Override
    public String decode(String currency) {
        switch (currency) {
            case "USDT":
            case "$":
                return "USD";
            default:
                return currency;
        }
    }
}
