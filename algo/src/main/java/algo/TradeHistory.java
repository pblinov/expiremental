package algo;

import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class TradeHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeHistory.class);
    private final TradeService tradeService;
    private final String exchange;
    private final Collection<TradeHistoryParams> params;

    public TradeHistory(String exchange, TradeService tradeService, Collection<TradeHistoryParams> params) {
        this.tradeService = tradeService;
        this.exchange = exchange;
        this.params = params;
    }

    public void print() {
        params.forEach(param -> {
            try {
                final UserTrades trades = tradeService.getTradeHistory(param);
                trades.getUserTrades().forEach(trade -> {
                    LOGGER.info("{} {} {}@{} fee {} {}", exchange, trade.getCurrencyPair(), trade.getOriginalAmount(), trade.getPrice(), trade.getFeeAmount(), trade.getFeeCurrency());
                });
            } catch (Exception e) {
                LOGGER.warn("Cannot read trades", e);
            }
        });
    }
}
