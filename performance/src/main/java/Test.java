import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.transport.ClientTransport;
import org.cometd.websocket.client.WebSocketTransport;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Test {
    private static Logger LOGGER = LoggerFactory.getLogger(Test.class);

    public static void main(String [] args) throws Exception {
        Map<String, Object> options = new HashMap<String, Object>();
        WebSocketClientFactory factory = new WebSocketClientFactory();
        factory.start();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(100);
        ClientTransport transport = new WebSocketTransport(options, factory, executorService);

        LOGGER.info("1");

        //ClientSession client = new BayeuxClient("ws://195.206.225.50:8081/fbms/ws", transport);
        ClientSession client = new BayeuxClient("ws://127.0.0.1:8080/fbms/ws", transport);
        client.handshake(null, new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                LOGGER.info("channel={}, message={}", channel.getId(), message.getJSON());
            }
        });

        LOGGER.info("client.isConnected={}", client.isConnected());

        client.getChannel(Channel.META_CONNECT).addListener(new ClientSessionChannel.MessageListener() {
            @Override
            public void onMessage(ClientSessionChannel channel, Message message) {
                LOGGER.trace("channel={}, message={}", channel.getId(), message.getJSON());
            }
        });

        for (int i = 10; i < 60; i++) {
            ClientSessionChannel marketDataChannel = client.getChannel(String.format("/market_data/%s", i));
            marketDataChannel.subscribe(new ClientSessionChannel.MessageListener() {
                @Override
                public void onMessage(ClientSessionChannel channel, Message message) {
                    LOGGER.trace("channel={}, message={}", message.getJSON());
                    JSONParser parser = new JSONParser();
                    try {

                        JSONObject jsonObject = (JSONObject) parser.parse(message.getJSON());
                        JSONArray data = (JSONArray) jsonObject.get("data");
                        for (int i = 0; i < data.size(); i++) {
                            JSONObject item = (JSONObject) data.get(i);
                            MonitorFactory.add("diff", "ms", System.currentTimeMillis() - (Long) item.get("receivedTimestamp"));
//                            LOGGER.info("delta={}/{}, underlying={}, optionId={}, type={}, receivedTimestamp={}",
//                                    System.currentTimeMillis() - (Long) item.get("receivedTimestamp"),
//                                    System.currentTimeMillis() - (Long) item.get("timestamp"),
//                                    item.get("underlying"), item.get("optionId"),
//                                    item.get("type"), item.get("receivedTimestamp"));
                        }

                    } catch (ParseException e) {
                        LOGGER.error("Cannot parse: {}", message.getJSON());
                    }

                }
            });
        }

        while (true) {
            Thread.sleep(10000);
            Monitor monitor = MonitorFactory.getMonitor("diff", "ms");
            LOGGER.info("{}/{}/{} - {}",
                    monitor.getMin(),
                    monitor.getAvg(),
                    monitor.getMax(),
                    monitor.getHits());
            monitor.reset();
        }

        //LOGGER.info("client.isConnected={}", client.isConnected());
    }
}
