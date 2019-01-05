package algo;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public abstract class MarketData {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarketData.class);
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USD = "USD";
    public static final String RUB = "RUB";
    public static final String BNB = "BNB";

    private final Balances balances;
    protected final Portfolio portfolio;
    private final Converters converterService;
    private final TradeHistory tradeHistory;
    protected final Exchange exchange;

    public MarketData(String apiKey, String secretKey, Portfolio portfolio) throws IOException {
        this.portfolio = portfolio;
        exchange = createExchange(apiKey, secretKey);
        balances = new BalanceCache(getName(), getSymbolConverter(), exchange.getAccountService());

        MarketDataService marketDataService = exchange.getMarketDataService();
        ExchangeMetaData metaData = exchange.getExchangeMetaData();
        converterService = new ConverterService(getName(), marketDataService, metaData, portfolio.currencies(), getSymbolConverter());

        tradeHistory = new TradeHistory(getName(), exchange.getTradeService(), getTradeHistoryParams(), getSymbolConverter(), getPositionThreshold());
    }

    protected Date getPositionThreshold() {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2018);
        calendar.set(Calendar.MONTH, Calendar.SEPTEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 15);
        return calendar.getTime();
    }

    protected abstract Collection<TradeHistoryParams> getTradeHistoryParams();

    static boolean isMain(String currency) {
        return currency.equals(BTC) || currency.equals(USD);
    }

    private Exchange createExchange(String apiKey, String secretKey) {
        ExchangeSpecification specification = createSpecification(apiKey, secretKey);
        return ExchangeFactory.INSTANCE.createExchange(specification);
    }

    protected List<CurrencyPair> pairs(String currency) {
        return portfolio.currencies().stream()
                .map(c -> new CurrencyPair(getSymbolConverter().encode(c), getSymbolConverter().encode(currency)))
                .filter(pair -> exchange.getExchangeMetaData().getCurrencyPairs().containsKey(pair))
                .collect(Collectors.toList());
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

    public TradeHistory getTradeHistory() {
        return tradeHistory;
    }
}
