package marketdata.transport;

import java.math.BigDecimal;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class Quote {
    private QuoteType type;
    private String symbol;
    private String exchange;
    private BigDecimal price;
    private Long size;
    private Long timestamp;
    private Long receivedTimestamp;

    public Quote() {
    }

    public Quote(QuoteType type, String symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public QuoteType getType() {
        return type;
    }

    public void setType(QuoteType type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public void setReceivedTimestamp(Long receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }
}
