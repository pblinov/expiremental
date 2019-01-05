package algo;

public interface Converters {
    Converter getConverter(String base, String quote);
    String getExchange();
}
