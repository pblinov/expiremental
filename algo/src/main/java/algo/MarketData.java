package algo;

import algo.history.TradeHistory;
import algo.history.TradeHistoryWriter;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class MarketData {
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";
    public static final String USD = "USD";
    public static final String USDF = "USDF";
    public static final String USDT = "USDT";
    public static final String BUSD = "BUSD";
    public static final String RUB = "RUB";
    public static final String BNB = "BNB";

    private final Balances balances;
    protected final Portfolio portfolio;
    private final Converters converterService;
    private final TradeHistory tradeHistory;
    protected final Exchange exchange;

    public MarketData(String apiKey, String secretKey, Portfolio portfolio,
                      TradeHistoryWriter tradeHistoryWriter,
                      BalanceWriter balanceWriter) throws IOException {
        this.portfolio = portfolio;
        exchange = createExchange(apiKey, secretKey);

        MarketDataService marketDataService = exchange.getMarketDataService();
        ExchangeMetaData metaData = exchange.getExchangeMetaData();
        Collection<String> currencies = new ArrayList<>(portfolio.currencies());
        //currencies.add("USDF");
        converterService = new ConverterService(getName(), marketDataService, metaData, currencies, getSymbolConverter());
        balances = new BalanceCache(getName(), getSymbolConverter(), exchange.getAccountService(), balanceWriter, converterService);
        tradeHistory = new TradeHistory(getName(), exchange.getTradeService(), getTradeHistoryParams(), getSymbolConverter(), getPositionThreshold(), tradeHistoryWriter);
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
        return currency.equals(BTC) || currency.equals(USD) || currency.equals(USDF) || currency.equals(USDT) || currency.equals(BUSD);
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

    public Exchange getExchange() {
        return exchange;
    }
}
