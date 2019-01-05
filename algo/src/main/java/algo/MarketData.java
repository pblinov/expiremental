package algo;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;

public abstract class MarketData {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketData.class);
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USD = "USD";
    public static final String RUB = "RUB";

    private final Balances balances;
    private final Portfolio portfolio;
    private final Converters converterService;
    private double totalFee = 0.0;

    public MarketData(String apiKey, String secretKey, Portfolio portfolio) throws IOException {
        this.portfolio = portfolio;
        Exchange exchange = createExchange(apiKey, secretKey);
        balances = new BalanceCache(getName(), getSymbolConverter(), exchange.getAccountService());

        MarketDataService marketDataService = exchange.getMarketDataService();
        ExchangeMetaData metaData = exchange.getExchangeMetaData();
        converterService = new ConverterService(getName(), marketDataService, metaData, portfolio.currencies(), getSymbolConverter());
    }

    static boolean isMain(String currency) {
        return currency.equals(BTC) || currency.equals(USD);
    }

    private Exchange createExchange(String apiKey, String secretKey) {
        ExchangeSpecification specification = createSpecification(apiKey, secretKey);
        return ExchangeFactory.INSTANCE.createExchange(specification);
    }

    protected abstract ExchangeSpecification createSpecification(String apiKey, String secretKey);
    protected abstract SymbolConverter getSymbolConverter();
    protected abstract String getName();

    public Balances getBalances() {
        return balances;
    }

    public Converters getConverterService() {
        return converterService;
    }
}
