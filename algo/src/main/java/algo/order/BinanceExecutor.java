package algo.order;

import algo.exchange.BinanceMarketData;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static java.lang.String.format;

@Slf4j
public class BinanceExecutor implements OrderExecutor {
    private final BinanceMarketData marketData;
    private final Exchange exchange;

    public BinanceExecutor(BinanceMarketData marketData) {
        this.marketData = marketData;
        this.exchange = marketData.getExchange();
    }

    @Override
    public void executeOrder(double baseQty, double quoteQty, String currency, String toCurrency, double deltaInUsd, double expectedFee) {
        log.info("[ACTION] {}", format("%.8f %s > %.8f %s (diff: $%.1f, fee: %.8f)", baseQty, currency, quoteQty, toCurrency, deltaInUsd, expectedFee));
        var marketOrder = new MarketOrder(baseQty > 0 ? Order.OrderType.BID : Order.OrderType.ASK,
                new BigDecimal(Math.abs(baseQty), new MathContext(8, RoundingMode.HALF_UP)).stripTrailingZeros(),
                new CurrencyPair(marketData.encode(currency),
                marketData.encode(toCurrency)));
        log.info("Order: {}", marketOrder);
        try {
            exchange.getTradeService().placeMarketOrder(marketOrder);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot place order", e);
        }
    }
}
