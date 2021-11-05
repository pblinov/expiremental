package algo;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PriceCache {
    private final Map<String, BigDecimal> prices = new HashMap<>();

    private final MarketDataService marketDataService;
    private final ExchangeMetaData metaData;
    private final String balanceCurrency;
    private final SymbolConverter symbolConverter;

    public PriceCache(MarketDataService marketDataService, ExchangeMetaData metaData, String balanceCurrency, SymbolConverter symbolConverter) {
        this.marketDataService = marketDataService;
        this.metaData = metaData;
        this.balanceCurrency = balanceCurrency;
        this.symbolConverter = symbolConverter;
    }

    public BigDecimal get(String currency) {
        return prices.computeIfAbsent(currency, this::request);
    }

    private BigDecimal request(String currency) {
        final String encodedCurrency = symbolConverter.encode(currency);
        final String balanceCurrency = symbolConverter.encode(this.balanceCurrency);
        try {
            if (metaData.getCurrencyPairs().containsKey(new CurrencyPair(encodedCurrency, balanceCurrency))) {
                log.info("{} minQty: {}", encodedCurrency, metaData.getCurrencyPairs().get(new CurrencyPair(encodedCurrency, balanceCurrency)).getMinimumAmount());
                Ticker ticker = marketDataService.getTicker(new CurrencyPair(encodedCurrency, balanceCurrency));
                return ticker.getAsk();
            } else if (metaData.getCurrencyPairs().containsKey(new CurrencyPair(balanceCurrency, encodedCurrency))) {
                Ticker ticker = marketDataService.getTicker(new CurrencyPair(balanceCurrency, encodedCurrency));
                return BigDecimal.ONE.divide(ticker.getAsk(), MathContext.DECIMAL32);
            } else if (encodedCurrency.equals(balanceCurrency)) {
                return BigDecimal.ONE;
            } else if (balanceCurrency.equals(symbolConverter.encode("USD"))) {
                Ticker tickerBTC = marketDataService.getTicker(new CurrencyPair(encodedCurrency, symbolConverter.encode("BTC")));
                Ticker tickerBTCUSD = marketDataService.getTicker(new CurrencyPair(symbolConverter.encode("BTC"), symbolConverter.encode("USD")));
                return tickerBTC.getAsk().multiply(tickerBTCUSD.getAsk());
            } else {
                return BigDecimal.ZERO;
            }
        } catch (IOException e) {
            log.error("Cannot find price of {}", encodedCurrency);
            return BigDecimal.ZERO;
        }
    }
}
