package algo;

public interface ConverterServiceInterface {
    Converter getConverter(String base, String quote);
    String getExchange();
}
