package algo;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;

public class BalanceWriter implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(BalanceWriter.class);

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
            LOGGER.warn("Cannot write balance", e);
        }
    }
}
