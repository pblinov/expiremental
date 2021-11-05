package algo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.knowm.xchange.dto.trade.UserTrade;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Slf4j
public class TradeHistoryWriter implements Closeable {
    private final DateFormat format = new SimpleDateFormat("YYYY-MM-dd");
    private final CSVPrinter printer;

    public TradeHistoryWriter() throws IOException {
        printer = new CSVPrinter(new FileWriter("trades.csv", false), CSVFormat.EXCEL);
        printer.printRecord(
                "Exchange",
                "Instrument",
                "Type",
                "Qty",
                "Price",
                "ID",
                "Fee",
                "Fee Currency",
                "Timestamp"
        );
    }

    @Override
    public void close() throws IOException {
        printer.close();
    }

    public void write(String exchange, UserTrade trade) {
        try {
            printer.printRecord(
                    exchange,
                    trade.getCurrencyPair(),
                    trade.getType(),
                    trade.getOriginalAmount(),
                    trade.getPrice(),
                    trade.getId(),
                    trade.getFeeAmount(),
                    trade.getFeeCurrency(),
                    format.format(trade.getTimestamp())
            );
        } catch (IOException e) {
            log.warn("{} Cannot write trade: {}", exchange, trade, e);
        }
    }
}
