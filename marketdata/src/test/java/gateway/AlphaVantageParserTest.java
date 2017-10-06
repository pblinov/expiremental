package gateway;

import marketdata.gateway.AlphaVantageParser;
import marketdata.transport.Quote;
import marketdata.transport.QuoteType;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author pblinov
 * @since 05/10/2017
 */
public class AlphaVantageParserTest {
    @Test
    public void testCsvParser() {
        InputStream stream = getClass().getResourceAsStream("alphavantage.csv");
        assertNotNull(stream);
        List<Quote> quotes = AlphaVantageParser.parse("AAPL", stream);
        assertEquals(200, quotes.size());

        Quote quote = quotes.get(0);
        assertEquals("AAPL", quote.getSymbol());
        assertEquals("2016-10-04 04:12:00", new SimpleDateFormat("yyyy-mm-dd hh:MM:ss").format(quote.getTimestamp()));
        assertEquals(QuoteType.BID, quote.getType());
        assertEquals(Long.valueOf(73212), quote.getSize());
        assertEquals(new BigDecimal("951.9100"), quote.getPrice());

        quote = quotes.get(1);
        assertEquals("AAPL", quote.getSymbol());
        assertEquals("2016-10-04 04:12:00", new SimpleDateFormat("yyyy-mm-dd hh:MM:ss").format(quote.getTimestamp()));
        assertEquals(QuoteType.ASK, quote.getType());
        assertEquals(Long.valueOf(73212), quote.getSize());
        assertEquals(new BigDecimal("950.9400"), quote.getPrice());
    }
}
