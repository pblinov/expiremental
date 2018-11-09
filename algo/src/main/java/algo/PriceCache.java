package algo;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

public class PriceCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceCache.class);

    private final Map<String, BigDecimal> prices = new HashMap<>();

    private final MarketDataService marketDataService;
    private final ExchangeMetaData metaData;
    private final String balanceCurrency;

    public PriceCache(MarketDataService marketDataService, ExchangeMetaData metaData, String balanceCurrency) {
        this.marketDataService = marketDataService;
        this.metaData = metaData;
        this.balanceCurrency = balanceCurrency;
    }

    public BigDecimal get(String currency) {
        return prices.computeIfAbsent(currency, this::request);
    }

    private BigDecimal request(String currency) {
        try {
            if (metaData.getCurrencyPairs().containsKey(new CurrencyPair(currency, balanceCurrency))) {
                LOGGER.info("{} minQty: {}", currency, metaData.getCurrencyPairs().get(new CurrencyPair(currency, balanceCurrency)).getMinimumAmount());
                Ticker ticker = marketDataService.getTicker(new CurrencyPair(currency, balanceCurrency));
                return ticker.getAsk();
            } else if (metaData.getCurrencyPairs().containsKey(new CurrencyPair(balanceCurrency, currency))) {
                Ticker ticker = marketDataService.getTicker(new CurrencyPair(balanceCurrency, currency));
                return BigDecimal.ONE.divide(ticker.getAsk(), MathContext.DECIMAL32);
            } else if (currency.equals(balanceCurrency)) {
                return BigDecimal.ONE;
            } else if (balanceCurrency.equals("USDT")) {
                Ticker tickerBTC = marketDataService.getTicker(new CurrencyPair(currency, "BTC"));
                Ticker tickerBTCUSD = marketDataService.getTicker(new CurrencyPair("BTC", "USDT"));
                return tickerBTC.getAsk().multiply(tickerBTCUSD.getAsk());
            } else {
                return BigDecimal.ZERO;
            }
        } catch (IOException e) {
            LOGGER.error("Cannot find price of {}", currency);
            return BigDecimal.ZERO;
        }
    }
}
