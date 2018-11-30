package algo;

import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exmo.ExmoExchange;

import java.io.IOException;

public class ExmoMarketData extends MarketData implements SymbolConverter {
    public ExmoMarketData(String apiKey, String secretKey) throws IOException {
        super(apiKey, secretKey);
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
    public String encode(String currency) {
        return currency;
    }

    @Override
    public String decode(String currency) {
        return currency;
    }
}
