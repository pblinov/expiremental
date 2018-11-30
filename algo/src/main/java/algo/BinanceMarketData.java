package algo;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;

import java.io.IOException;

public class BinanceMarketData extends MarketData implements SymbolConverter {
    public BinanceMarketData(String apiKey, String secretKey) throws IOException {
        super(apiKey, secretKey);
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
    public String encode(String currency) {
        switch (currency) {
            case "USD":
                return "USDT";
            default:
                return currency;
        }
    }

    @Override
    public String decode(String currency) {
        switch (currency) {
            case "USDT":
                return "USD";
            default:
                return currency;
        }
    }
}
