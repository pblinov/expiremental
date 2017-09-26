package disruptor;

import com.lmax.disruptor.EventFactory;

public class QuoteValue {
    private int value;

    public final static EventFactory<QuoteValue> EVENT_FACTORY = QuoteValue::new;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
