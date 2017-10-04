package marketdata.transport;

import com.sun.org.apache.xpath.internal.operations.Quo;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public interface QuoteListener {
    void onQuote(Quote quote);
}
