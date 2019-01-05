package algo;

import static java.lang.String.format;

public class Position {
    private final Instrument instrument;
    private double closedPosition;
    private double openPosition;

    public Position(Instrument instrument) {
        this.instrument = instrument;
    }

    public void add(double qty, double price) {
        closedPosition -= qty * price;
        openPosition += qty;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public double getClosedPosition() {
        return closedPosition;
    }

    public double getOpenPosition() {
        return openPosition;
    }

    @Override
    public String toString() {
        return format("Position{%s %f %s %f}", instrument.getBase(), openPosition, instrument.getQuote(), closedPosition);
    }
}
