package algo.history;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.UserTrade;

import java.io.IOException;
import java.sql.*;
import java.util.Locale;

@Slf4j
public class TradeHistoryWriterToDB implements TradeHistoryWriter {
    private final Connection connection;

    public TradeHistoryWriterToDB() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot init PG JDBC driver");
        }

        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost/invest", "dev", "dev");
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create JDBC connection", e);
        }
    }

    @Override
    public void write(String exchange, UserTrade trade) {
        log.debug("Save: {}", trade);
        final var account = exchange.toUpperCase(Locale.ROOT);
        final var pair = (CurrencyPair) trade.getInstrument();
        try (var find = connection.prepareStatement("SELECT * FROM trades WHERE account = ? AND code = ?")) {
            find.setString(1, account);
            find.setString(2, trade.getId());
            var findResult = find.executeQuery();
            if (!findResult.next()) {
                try (var insert = connection.prepareStatement("INSERT INTO trades (account, code, \"date\", qty, price, currency, instrument, fee, fee_currency) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    insert.setString(1, account);
                    insert.setString(2, trade.getId());
                    insert.setDate(3, new java.sql.Date(trade.getTimestamp().getTime()));
                    insert.setBigDecimal(4, trade.getOriginalAmount());
                    insert.setBigDecimal(5, trade.getPrice());
                    insert.setString(6, pair.counter.getSymbol());
                    insert.setString(7, pair.toString());
                    insert.setBigDecimal(8, trade.getFeeAmount());
                    insert.setString(9, trade.getFeeCurrency().getSymbol());

                    insert.execute();
                    log.info("New trade persisted: {}", trade);
                } catch (SQLException e) {
                    log.error("Cannot persist {}", trade, e);
                }
            }
            findResult.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create statement", e);
        }
    }

    @Override
    public void close() {
        log.debug("Close DB connection");
        try {
            connection.close();
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot close JDBC connection", e);
        }
    }
}
