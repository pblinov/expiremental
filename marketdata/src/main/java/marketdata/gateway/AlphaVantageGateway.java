package marketdata.gateway;

import marketdata.transport.Quote;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.*;

import static marketdata.gateway.AlphaVantageParser.createFormatter;

/**
 * @author pblinov
 * @since 05/10/2017
 */
public class AlphaVantageGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaVantageGateway.class);

    private static String URL_TEMPLATE = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol={0}&interval=1min&apikey={1}&datatype=csv";
    private static String[] symbols = {"BAC", "F", "GE", "TWTR", "T", "C", "SNAP", "GM", "MSFT", "RAD", "HPQ", "ORCL", "VZ", "CVS", "AAPL", "GOOG", "DB"};
    static {
        Arrays.sort(symbols);
        for (String symbol : symbols) {
            System.out.print(symbol + ", ");
        }
    }

    private String key;
    private Map<String, Long> lastTimestamps = new HashMap<>();

    public static void main(String[] args) throws IOException {
        AlphaVantageGateway gateway = new AlphaVantageGateway("SUHFH5L4JEFMI7OT");
        gateway.start();
    }

    public AlphaVantageGateway(String key) {
        this.key = key;
    }

    private String createUrl(String symbol) {
        return MessageFormat.format(URL_TEMPLATE, symbol, key);
    }

    public void start() throws IOException {
        LOGGER.info("Symbol counter: {}", symbols.length);
        for (int i = 0; i < 10; i++) {
            for (String symbol : symbols) {
                String url = createUrl(symbol);
                LOGGER.trace(url);

                consumeQuotes(url, symbol);
            }
            try {
                Thread.sleep(30_000L);
            } catch (InterruptedException e) {
                LOGGER.error("Cannot sleep", e);
            }
        }
    }

    private void consumeQuotes(String url, String symbol) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet request = new HttpGet(url);

        CloseableHttpResponse response = client.execute(request, context);

        try {
            LOGGER.trace("Status: {}", response.getStatusLine());
            HttpEntity entity = response.getEntity();
            List<Quote> quotes = AlphaVantageParser.parse(symbol, entity.getContent());
            EntityUtils.consume(entity);
            print(symbol, quotes);
        } finally {
            response.close();
        }
    }

    private void print(String symbol, List<Quote> quotes) {
        Long lastTimestamp = lastTimestamps.get(symbol);
        if (lastTimestamp == null) {
            lastTimestamp = 0L;
        }
        long newLastTimestamp = 0L;
        for (Quote quote : quotes) {
            if (quote.getTimestamp() > lastTimestamp) {
                newLastTimestamp = quote.getTimestamp();
                LOGGER.info("{} {} {} {} {}", quote.getSymbol(), formatTime(quote.getTimestamp()), quote.getType(), quote.getPrice(), quote.getSize());
            }
        }
        if (newLastTimestamp > 0) {
            lastTimestamps.put(symbol, newLastTimestamp);
        }
    }

    private String formatTime(long timestamp) {
        return createFormatter().format(new Date(timestamp));
    }
}
