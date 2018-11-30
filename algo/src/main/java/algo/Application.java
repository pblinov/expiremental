package algo;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException {
        MarketData binance = new BinanceMarketData(args[0], args[1]);
        binance.run();
        MarketData exmo = new ExmoMarketData(args[2], args[3]);
        exmo.run();
        MarketData hitbtc = new HitbtcMarketData(args[4], args[5]);
        hitbtc.run();
    }
}
