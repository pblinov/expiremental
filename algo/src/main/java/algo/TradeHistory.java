package algo;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParams;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TradeHistory {
    private final TradeService tradeService;
    private final String exchange;
    private final Collection<TradeHistoryParams> params;
    private final SymbolConverter symbolConverter;
    private final Date threshold;
    private final TradeHistoryWriter tradeHistoryWriter;

    public TradeHistory(String exchange,
                        TradeService tradeService,
                        Collection<TradeHistoryParams> params,
                        SymbolConverter symbolConverter,
                        Date threshold,
                        TradeHistoryWriter tradeHistoryWriter) {
        this.tradeService = tradeService;
        this.exchange = exchange;
        this.params = params;
        this.symbolConverter = symbolConverter;
        this.threshold = threshold;
        this.tradeHistoryWriter = tradeHistoryWriter;
    }

    public Collection<Position> positions() {
        return params.stream()
                .flatMap(param -> {
                    try {
                        final UserTrades trades = tradeService.getTradeHistory(param);
                        final Map<Instrument, Position> result = new HashMap<>();
                        trades.getUserTrades().stream()
                                .peek(trade -> tradeHistoryWriter.write(exchange, trade))
                                .filter(trade -> threshold.before(trade.getTimestamp()))
                                .forEach(trade -> {
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
                        log.warn("Cannot read trades", e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());
    }
}
