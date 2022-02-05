package algo;

import algo.exchange.BinanceMarketData;
import algo.exchange.ExmoMarketData;
import algo.exchange.HitbtcMarketData;
import algo.history.TradeHistoryWriter;
import algo.history.TradeHistoryWriterToDB;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static algo.MarketData.*;
import static java.util.Arrays.asList;

@Slf4j
public class TradeLoader {
    public static void main(String[] args) {
        try {
            final var binanceApiKey = args[0];
            final var binanceSecretKey = args[1];
            final var exmoApiKey = args[2];
            final var exmoSecretKey = args[3];
            final var hitbtcApiKey = args[4];
            final var hitbtcSecretKey = args[5];

            final Portfolio portfolio = new SimplePortfolio(asList(BTC, ETH, USD, BUSD, USDT, RUB, "BCH", BNB, "EOS", "LTC", "NEO", "XMR", "TRX", "ADA", "XLM", "XRP", "TONCOIN", "SOL", "DOT"));
            final TradeHistoryWriter tradeHistoryWriter = new TradeHistoryWriterToDB();
            final BalanceWriter balanceWriter = new BalanceWriter();

            asList(
                    new BinanceMarketData(binanceApiKey, binanceSecretKey, portfolio, tradeHistoryWriter, balanceWriter),
                    new ExmoMarketData(exmoApiKey, exmoSecretKey, portfolio, tradeHistoryWriter, balanceWriter),
                    new HitbtcMarketData(hitbtcApiKey, hitbtcSecretKey, portfolio, tradeHistoryWriter, balanceWriter)
            ).forEach(marketData -> {
                log.info("Loading trades from {}...", marketData.getName());
                marketData.getTradeHistoryParams().forEach(param -> {
                    try {
                        marketData.getExchange()
                                .getTradeService()
                                .getTradeHistory(param)
                                .getUserTrades()
                                .forEach(trade -> tradeHistoryWriter.write(marketData.getName(), trade));
                    } catch (IOException e) {
                        log.error("Cannot load trade history", e);
                    }
                });
            });

            tradeHistoryWriter.close();
            balanceWriter.close();
        } catch (IOException e) {
            log.error("Cannot execute trade loader", e);
            System.exit(1);
        }
    }
}
