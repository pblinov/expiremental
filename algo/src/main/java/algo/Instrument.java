package algo;

import com.sun.istack.internal.NotNull;

import static java.lang.String.format;

public class Instrument {
    private final String base;
    private final String quote;

    public Instrument(@NotNull String base, @NotNull String quote) {
        this.base = base;
        this.quote = quote;
    }

    public String getBase() {
        return base;
    }

    public String getQuote() {
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Instrument that = (Instrument) o;

        if (!base.equals(that.base)) return false;
        return quote.equals(that.quote);
    }

    @Override
    public int hashCode() {
        int result = base.hashCode();
        result = 31 * result + quote.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return format("%s/%s", base, quote);
    }
}
