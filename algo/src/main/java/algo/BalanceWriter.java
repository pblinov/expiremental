package algo;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class BalanceWriter implements Closeable {
    private final CSVPrinter printer;

    public BalanceWriter() throws IOException {
        printer = new CSVPrinter(new FileWriter("balances.csv", false), CSVFormat.EXCEL);
        printer.printRecord(
                "Exchange",
                "Wallet",
                "Symbol",
                "Qty",
                "BTC Qty",
                "USD Qty"
        );
    }

    @Override
    public void close() throws IOException {
        printer.close();
    }

    public void write(String exchange, String walletName, String symbol, double qty, double btcQty, double usdQty) {
        try {
            printer.printRecord(
                    exchange,
                    walletName,
                    symbol,
                    qty,
                    btcQty,
                    usdQty
            );
        } catch (IOException e) {
            log.warn("Cannot write balance", e);
        }
    }
}
