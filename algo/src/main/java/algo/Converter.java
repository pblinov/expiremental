package algo;

public interface Converter {
    double convert(double qty);

    Converter reverse();

    double round(double expectedQty);
}
