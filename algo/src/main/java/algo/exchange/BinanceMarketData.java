package algo.exchange;

import algo.*;
import algo.history.TradeHistoryWriter;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.service.BinanceTradeHistoryParams;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BinanceMarketData extends MarketData implements SymbolConverter {
    public BinanceMarketData(String apiKey, String secretKey, Portfolio portfolio, TradeHistoryWriter tradeHistoryWriter, BalanceWriter balanceWriter) throws IOException {
        super(apiKey, secretKey, portfolio, tradeHistoryWriter, balanceWriter);
    }

    @Override
    protected Collection<TradeHistoryParams> getTradeHistoryParams() {
        return Stream.of(pairs(BTC), pairs(USD), pairs(ETH), pairs(BNB), pairs(BUSD))
                .flatMap(pairs -> pairs.stream().map(BinanceTradeHistoryParams::new))
                .collect(Collectors.toList());
    }

    @Override
    protected ExchangeSpecification createSpecification(String apiKey, String secretKey) {
        ExchangeSpecification specification = new ExchangeSpecification(BinanceExchange.class);
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
        return "Binance";
    }

    @Override
    public String encode(String currency) {
        switch (currency) {
            case "USD":
                return "USDT";
            case "BCH":
                return "BCH";
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
            case "BCH":
                return "BCH";
            default:
                return currency;
        }
    }
}
