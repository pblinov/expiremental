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
import java.util.function.Supplier;

public class ConverterService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterService.class);

    private final List<Converter> converters = new ArrayList<>();

    public ConverterService(MarketDataService marketDataService,
                            ExchangeMetaData metaData,
                            Collection<String> currencies) throws IOException {
        for (String base : currencies) {
            for (String quote : currencies) {
                final CurrencyPair pair = new CurrencyPair(base, quote);
                CurrencyPairMetaData data = metaData.getCurrencyPairs().get(pair);
                if (data != null) {
                    final Ticker ticker = marketDataService.getTicker(pair);
                    final Converter converter = new Converter(base, quote,
                            data.getMinimumAmount().doubleValue(),
                            ticker.getBid().doubleValue(),
                            ticker.getAsk().doubleValue());
                    converters.add(converter);
                    LOGGER.info("{}", converter);
                }
            }
        }
    }

    public Converter get(String base, String quote) {
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

        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    private Optional<Converter> find(String base, String quote) {
        return converters.stream()
                    .filter(c -> c.getQuoteCurrency().equals(quote))
                    .filter(c -> c.getBaseCurrency().equals(base))
                    .findFirst();
    }

    public double convert(double qty, String base, String quote) {
        return qty * get(base, quote).getAsk();
    }
}
