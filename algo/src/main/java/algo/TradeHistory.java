package algo;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TradeHistory {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeHistory.class);
    private final TradeService tradeService;
    private final String exchange;
    private final Collection<TradeHistoryParams> params;
    private final SymbolConverter symbolConverter;

    public TradeHistory(String exchange,
                        TradeService tradeService,
                        Collection<TradeHistoryParams> params,
                        SymbolConverter symbolConverter) {
        this.tradeService = tradeService;
        this.exchange = exchange;
        this.params = params;
        this.symbolConverter = symbolConverter;
    }

    public Collection<Position> positions() {
        return params.stream()
                .flatMap(param -> {
                    try {
                        final UserTrades trades = tradeService.getTradeHistory(param);
                        printTrades(trades);
                        final Map<Instrument, Position> result = new HashMap<>();
                        trades.getUserTrades().forEach(trade -> {
                            final Instrument instrument = new Instrument(
                                    symbolConverter.decode(trade.getCurrencyPair().base.getSymbol()),
                                    symbolConverter.decode(trade.getCurrencyPair().counter.getSymbol()));
                            final Position position = result.computeIfAbsent(instrument, Position::new);
                            final double quantity = trade.getOriginalAmount().doubleValue();
                            final double price = trade.getPrice().doubleValue();
                            position.add(
                                    trade.getType() == Order.OrderType.BID ? quantity : -quantity,
                                    price);
                        });
                        return result.values().stream();
                    } catch (IOException e) {
                        LOGGER.warn("Cannot read trades", e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }

    private void printTrades(UserTrades trades) {
        trades.getUserTrades().forEach(trade -> {
            LOGGER.debug("{} {} {} {}@{} #{} fee {} {} {}",
                    exchange,
                    trade.getCurrencyPair(),
                    trade.getType(),
                    trade.getOriginalAmount(),
                    trade.getPrice(),
                    trade.getId(),
                    trade.getFeeAmount(),
                    trade.getFeeCurrency(),
                    trade.getTimestamp());
        });
    }
}
