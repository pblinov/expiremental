package algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TwoStepConverter implements Converter {
    private final List<Converter> converters;

    public TwoStepConverter(List<Converter> converters) {
        this.converters = converters;
    }

    public double convert(double qty) {
        double result = qty;
        for (Converter converter : converters) {
            result = converter.convert(result);
        }
        return result;
    }

    @Override
    public Converter reverse() {
        final List<Converter> reverse = new ArrayList<>(converters);
        Collections.reverse(reverse);
        return new TwoStepConverter(reverse.stream().map(Converter::reverse).collect(Collectors.toList()));
    }

    @Override
    public double round(double expectedQty) {
        return converters.get(0).round(expectedQty);
    }
}
