package marketdata.transport;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public interface PublisherTransport {
    void send(Quote quote);
}
