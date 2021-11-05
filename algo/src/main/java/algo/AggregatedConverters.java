package algo;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class AggregatedConverters implements Converters {
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
                log.debug("{} Cannot convert {} to {}", converterService.getExchange(), base, quote);
            }
        }
        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    @Override
    public String getExchange() {
        return "Average";
    }
}
