import com.lmax.disruptor.EventFactory;

/**
 * @author pblinov
 * @since 10/02/2015
 */
public class Factory implements EventFactory<Event> {
    @Override
    public Event newInstance() {
        return new Event();
    }
}
