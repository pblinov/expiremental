package algo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AggregatedConverters implements Converters {
    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatedConverters.class);

    private final Collection<Converters> converterServices;

    public AggregatedConverters(Collection<Converters> converterServices) {
        this.converterServices = converterServices;
    }

    @Override
    public Converter getConverter(String base, String quote) {
        for (Converters converterService : converterServices) {
            try {
                return converterService.getConverter(base, quote);
            } catch (IllegalStateException e) {
                LOGGER.debug("{} Cannot convert {} to {}", converterService.getExchange(), base, quote);
            }
        }
        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    @Override
    public String getExchange() {
        return "Average";
    }
}
