import java.io.Serializable;

/**
 * @author pblinov
 * @since 10/02/2015
 */
public class Event implements Serializable {
    private long value;

    public void set(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Event{" +
                "value=" + value +
                '}';
    }

    public long getValue() {
        return value;
    }
}
