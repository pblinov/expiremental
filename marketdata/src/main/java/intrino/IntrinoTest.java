package intrino;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pblinov
 * @since 15/09/2017
 */
public class IntrinoTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(IntrinoTest.class);

    private static final String USERNMAE = "*";
    private static final String PASSWORD = "*";
    private static final String HOST = "api.intrinio.com";
    private static final String SECURITIES_URL = "https://" + HOST + "/securities";
    private static final String EXCHANGES_URL = "https://" + HOST + "/stock_exchanges?page_size=200";

    public static void main(String[] args) throws Exception {
        LOGGER.info("Start");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet request = new HttpGet(EXCHANGES_URL);
        //https://api.intrinio.com/stock_exchanges
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNMAE, PASSWORD);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope("api.intrinio.com", AuthScope.ANY_PORT), credentials);
        context.setCredentialsProvider(credentialsProvider);
        

        CloseableHttpResponse response = client.execute(request, context);

        try {
            System.out.println(response.getStatusLine());
            HttpEntity entity = response.getEntity();
            System.out.println(IOUtils.toString(entity.getContent()));
            EntityUtils.consume(entity);
        } finally {
            response.close();
        }


        LOGGER.info("Stop");
    }
}
