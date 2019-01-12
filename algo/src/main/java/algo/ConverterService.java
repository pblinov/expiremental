package algo;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static algo.MarketData.*;
import static java.util.Arrays.asList;

public class ConverterService implements Converters {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterService.class);

    private final List<SimpleConverter> converters = new ArrayList<>();
    private final String exchange;

    public ConverterService(String exchange,
                            MarketDataService marketDataService,
                            ExchangeMetaData metaData,
                            Collection<String> currencies,
                            SymbolConverter symbolConverter) throws IOException {
        this.exchange = exchange;
        for (String base : currencies) {
            for (String quote : currencies) {
                if (MarketData.isMain(base) || MarketData.isMain(quote)) {
                    final String encodedBase = symbolConverter.encode(base);
                    final String encodedQuote = symbolConverter.encode(quote);
                    final CurrencyPair pair = new CurrencyPair(encodedBase, encodedQuote);
                    final CurrencyPairMetaData data = metaData.getCurrencyPairs().get(pair);
                    if (data != null) {
                        try {
                            final Ticker ticker = marketDataService.getTicker(pair);
                            final SimpleConverter converter = new SimpleConverter(base, quote,
                                    data.getMinimumAmount().doubleValue(),
                                    ticker.getBid().doubleValue(),
                                    ticker.getAsk().doubleValue());
                            converters.add(converter);
                            LOGGER.debug("{}", converter);
                        } catch (IOException e) {
                            LOGGER.error("Cannot load ticker {}", pair);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Converter getConverter(String base, String quote) {
        if (base.equals(quote)) {
            return new SimpleConverter(base, quote, 1.0, 1.0, 1.0);
        }

        final Optional<SimpleConverter> direct = find(base, quote);
        if (direct.isPresent()) {
            return direct.get();
        }

        final Optional<SimpleConverter> reverse = find(quote, base);
        if (reverse.isPresent()) {
            return reverse.get().reverse();
        }

        if (USD.equals(quote)) {
            // When exchange support USD & USDT
            final Optional<SimpleConverter> usdFiat = find(base, USDF);
            if (usdFiat.isPresent()) {
                return usdFiat.get();
            }
        }

        if (USD.equals(quote) && !BTC.equals(base)) {
            final Optional<SimpleConverter> toBtc = find(base, BTC);
            if (toBtc.isPresent()) {
                final Optional<SimpleConverter> btcToBtc = find(BTC, USD);
                if (btcToBtc.isPresent()) {
                    return new TwoStepConverter(asList(toBtc.get(), btcToBtc.get()));
                }
            }
        }

        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    private Optional<SimpleConverter> find(String base, String quote) {
        return converters.stream()
                    .filter(c -> c.getQuoteCurrency().equals(quote))
                    .filter(c -> c.getBaseCurrency().equals(base))
                    .findFirst();
    }

    @Override
    public String getExchange() {
        return exchange;
    }
}
