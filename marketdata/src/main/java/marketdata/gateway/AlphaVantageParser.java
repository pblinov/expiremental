package marketdata.gateway;

import marketdata.transport.Quote;
import marketdata.transport.QuoteType;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.csv.CSVFormat.DEFAULT;

/**
 * @author pblinov
 * @since 05/10/2017
 */
public class AlphaVantageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaVantageParser.class);

    public static List<Quote> parse(String symbol, InputStream inputStream) {
        List<Quote> result = new ArrayList<>();

        try {
            CSVParser parser = CSVParser.parse(inputStream, defaultCharset(), DEFAULT.withFirstRecordAsHeader());
            for (CSVRecord record : parser) {
                result.add(parseBid(symbol, record));
                result.add(parseAsk(symbol, record));
            }
        } catch (Exception e) {
            LOGGER.error("Cannot parse", e);
        }

        Collections.reverse(result);
        return result;
    }

    private static Quote parseBid(String symbol, CSVRecord record) throws ParseException {
        Quote bidQuote = createQuote(QuoteType.BID, symbol, record);
        bidQuote.setPrice(decodePrice(record.get("high")));
        return bidQuote;
    }

    private static Quote parseAsk(String symbol, CSVRecord record) throws ParseException {
        Quote askQuote = createQuote(QuoteType.ASK, symbol, record);
        askQuote.setPrice(decodePrice(record.get("low")));
        return askQuote;
    }

    private static Quote createQuote(QuoteType type, String symbol, CSVRecord record) throws ParseException {
        Quote bidQuote = new Quote(type, symbol);
        bidQuote.setTimestamp(decodeTimestamp(record.get("timestamp")));
        bidQuote.setSize(decodeVolume(record.get("volume")));
        bidQuote.setReceivedTimestamp(System.currentTimeMillis());
        return bidQuote;
    }

    private static long decodeVolume(String volume) {
        return Long.parseLong(volume);
    }

    private static BigDecimal decodePrice(String high) {
        return new BigDecimal(high);
    }

    private static long decodeTimestamp(String timestamp) throws ParseException {
        return createFormatter().parse(timestamp).getTime();
    }

    public static SimpleDateFormat createFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
