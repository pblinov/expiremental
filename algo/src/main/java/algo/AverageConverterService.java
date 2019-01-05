package algo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class AverageConverterService implements ConverterServiceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(AverageConverterService.class);

    private final Collection<ConverterServiceInterface> converterServices;

    public AverageConverterService(Collection<ConverterServiceInterface> converterServices) {
        this.converterServices = converterServices;
    }

    @Override
    public Converter getConverter(String base, String quote) {
        for (ConverterServiceInterface converterService : converterServices) {
            try {
                return converterService.getConverter(base, quote);
            } catch (IllegalStateException e) {
                LOGGER.warn("{} Cannot convert {} to {}", converterService.getExchange(), base, quote);
            }
        }
        throw new IllegalStateException(String.format("Cannot convert %s to %s", base, quote));
    }

    @Override
    public String getExchange() {
        return "Average";
    }
}
