package algo.history;

import org.knowm.xchange.dto.trade.UserTrade;

import java.io.Closeable;

public interface TradeHistoryWriter extends Closeable {
    void write(String exchange, UserTrade trade);
}
