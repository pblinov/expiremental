package algo;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ConverterService implements Converters {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterService.class);

    private final List<Converter> converters = new ArrayList<>();
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
                    final CurrencyPair pair = new CurrencyPair(symbolConverter.encode(base), symbolConverter.encode(quote));
                    final CurrencyPairMetaData data = metaData.getCurrencyPairs().get(pair);
                    if (data != null) {
                        try {
                            final Ticker ticker = marketDataService.getTicker(pair);
                            final Converter converter = new Converter(base, quote,
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
            return new Converter(base, quote, 1.0, 1.0, 1.0);
        }

        final Optional<Converter> converter = find(base, quote);
        if (converter.isPresent()) {
            return converter.get();
        }

        final Optional<Converter> converter1 = find(quote, base);
        if (converter1.isPresent()) {
            return converter1.get().reverse();
        }

        if (MarketData.USD.equals(quote)) {
            // When exchange support USD & USDT
            final Optional<Converter> converter2 = find(base, "USDF");
            if (converter2.isPresent()) {
                return converter2.get();
            }
        }

        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    private Optional<Converter> find(String base, String quote) {
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
