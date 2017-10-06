package marketdata.gateway;

import marketdata.transport.Quote;
import org.apache.commons.io.IOUtils;
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
import java.text.MessageFormat;
import java.util.List;

/**
 * @author pblinov
 * @since 05/10/2017
 */
public class AlphaVantageGateway {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaVantageGateway.class);

    private static String URL_TEMPLATE = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol={0}&interval=1min&apikey={1}&datatype=csv";
    private static String[] symbols = {"BAC", "F", "GE", "TWTR", "T", "C", "SNAP", "GM", "MSFT", "RAD", "HPQ", "ORCL", "VZ", "CVS", "AAPL", "GOOG", "DB"};

    private String key;

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
        for (String symbol : symbols) {
            String url = createUrl(symbol);
            LOGGER.info(url);

            consumeQuotes(url, symbol);
        }
    }

    private void consumeQuotes(String url, String symbol) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet request = new HttpGet(url);

        CloseableHttpResponse response = client.execute(request, context);

        try {
            LOGGER.info("Status: {}", response.getStatusLine());
            HttpEntity entity = response.getEntity();
            List<Quote> quotes = AlphaVantageParser.parse(symbol, entity.getContent());
            EntityUtils.consume(entity);
            LOGGER.info("{} - {} - {}", symbol, quotes.size(), quotes.get(0).getPrice());
        } finally {
            response.close();
        }
    }
}
