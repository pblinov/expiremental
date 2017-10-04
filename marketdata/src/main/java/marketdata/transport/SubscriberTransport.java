package marketdata.transport;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public interface SubscriberTransport {
    void addListener(QuoteListener listener);
    void subscribe(String symbol);
    void unsubscribe(String symbol);
}
