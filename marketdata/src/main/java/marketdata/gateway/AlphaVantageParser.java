package marketdata.gateway;

import marketdata.transport.Quote;
import marketdata.transport.QuoteType;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        return result;
    }

    private static Quote parseBid(String symbol, CSVRecord record) throws ParseException {
        Quote bidQuote = new Quote();
        bidQuote.setSymbol(symbol);
        bidQuote.setTimestamp(decodeTimestamp(record.get("timestamp")));
        bidQuote.setType(QuoteType.BID);
        bidQuote.setPrice(decodePrice(record.get("high")));
        bidQuote.setSize(decodeVolume(record.get("volume")));
        return bidQuote;
    }

    private static Quote parseAsk(String symbol, CSVRecord record) throws ParseException {
        Quote bidQuote = new Quote();
        bidQuote.setSymbol(symbol);
        bidQuote.setTimestamp(decodeTimestamp(record.get("timestamp")));
        bidQuote.setType(QuoteType.ASK);
        bidQuote.setPrice(decodePrice(record.get("low")));
        bidQuote.setSize(decodeVolume(record.get("volume")));
        return bidQuote;
    }

    private static long decodeVolume(String volume) {
        return Long.parseLong(volume);
    }

    private static BigDecimal decodePrice(String high) {
        return new BigDecimal(high);
    }

    private static long decodeTimestamp(String timestamp) throws ParseException {
        return new SimpleDateFormat("yyyy-mm-dd hh:MM:ss").parse(timestamp).getTime();
    }
}
