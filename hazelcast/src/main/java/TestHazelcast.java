import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * @author pblinov
 * @since 12/03/2015
 */
public class TestHazelcast {
    private static final Logger LOGGER = Logger.getLogger(TestHazelcast.class.getName());
    public static final int COUNTER = 1000000;

    public static void main(String [] args) {
        LOGGER.info(String.format("JVM: %s", System.getProperty("java.version")));


        MapConfig mapConfig = new MapConfig();
        mapConfig.setName("customers");
        mapConfig.setBackupCount(0);
        mapConfig.setAsyncBackupCount(0);
        mapConfig.getMaxSizeConfig().setSize(200000);
        mapConfig.setOptimizeQueries(true);
        mapConfig.setInMemoryFormat(InMemoryFormat.OBJECT);

        Config config = new Config();
        config.setInstanceName("my-instance");
        config.addMapConfig( mapConfig );

        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        final Map<Integer, TestObject1> customers = hazelcastInstance.getMap( "customers" );

        LOGGER.info("Start PUT");
        Executor putExecutor = Executors.newCachedThreadPool();
        final int grpCount = 5;
        for (int j = 0; j < grpCount; j++) {
            final int offset = j * COUNTER / grpCount;
            putExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    final long start = System.currentTimeMillis();
                    for (int i = 0; i < COUNTER / grpCount; i++) {
                        int key = offset + i;
                        customers.put(key, new TestObject1(key));
                    }
                    LOGGER.info("PUT: " + (System.currentTimeMillis() - start) / (double) COUNTER + " / " + customers.size());
                }
            });
        }


//        Executor getExecutor = Executors.newSingleThreadExecutor();
//        getExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                final long start = System.currentTimeMillis();
//                for (int i = 0; i < COUNTER; i++) {
//                    LOGGER.fine(customers.get(i));
//                }
//                LOGGER.info("GET: " + (System.currentTimeMillis() - start) / (double) COUNTER + " / " + customers.size());
//            }
//        });


        final BlockingQueue<String> queueCustomers = hazelcastInstance.getQueue( "customers" );

        Executor offerExecutor = Executors.newSingleThreadExecutor();
        offerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                for (int i = 0; i < COUNTER; i++) {
                    queueCustomers.offer(name(i));
                }
                LOGGER.info("OFFER: " + (System.currentTimeMillis() - start) / (double) COUNTER + " / " + queueCustomers.size());
            }
        });

        Executor pollExecutor = Executors.newSingleThreadExecutor();
        pollExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final long startPolling = System.currentTimeMillis();
                for (int i = 0; i < COUNTER; i++) {
                    try {
                        queueCustomers.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                LOGGER.info("TAKE: " + (System.currentTimeMillis() - startPolling) / (double) COUNTER + " / " + queueCustomers.size());
            }
        });

        try {
            Thread.sleep(300000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String name(int i) {
        return String.format("Customer #%d", i);
    }

    public static TestObject obj(int i) {
        return new TestObject(i);
    }

    private static class TestObject implements Serializable {
        public enum Mpv {
            PennyNickel('P'), NickelDime('S'), AlwaysPenny('E');

            private final char tradingSystemIdentifier;

            private Mpv(char tradingSystemIdentifier) {
                this.tradingSystemIdentifier = tradingSystemIdentifier;
            }

            public char getTradingSystemIdentifier() {
                return tradingSystemIdentifier;
            }
        }

        public enum Kind {
            CALL,
            PUT
        }

        private int optionId;

        private String underlying;

        private BigDecimal price;

        private Kind kind;

        private String root;

        private Date expirationDate;

        private Date lastTradingDate;

        private Character optionType;

        private String phlxSymbol;

        private Mpv mpv;

        private Boolean supportsTiedToStock;

        public TestObject(int i) {
            this.optionId = i;
            this.underlying = name(i);
            this.price = new BigDecimal(100 * i);
            this.kind = Kind.CALL;
            this.root = name(i);
            this.expirationDate = new Date();
            this.lastTradingDate = new Date();
            this.optionType = 'A';
            this.phlxSymbol = name(i);
            this.mpv = Mpv.AlwaysPenny;
            this.supportsTiedToStock = true;
        }

        public int getOptionId() {
            return optionId;
        }

        public void setOptionId(int optionId) {
            this.optionId = optionId;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Kind getKind() {
            return kind;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public Date getExpirationDate() {
            return (expirationDate == null) ? null : new Date(expirationDate.getTime());
        }

        public void setExpirationDate(Date expirationDate) {
            this.expirationDate = new Date(expirationDate.getTime());
        }

        public Date getLastTradingDate() {
            return (lastTradingDate == null) ? null : new Date(lastTradingDate.getTime());
        }

        public void setLastTradingDate(Date lastTradingDate) {
            this.lastTradingDate = new Date(lastTradingDate.getTime());
        }

        public String getUnderlying() {
            return underlying;
        }

        public void setUnderlying(String underlying) {
            this.underlying = underlying;
        }

        public String getStrikeMonth() {
            return String.format("%tb", getExpirationDate());
        }

        public String getStrikeDay() {
            return String.format("%td", getExpirationDate());
        }

        public String getStrikeYear() {
            return String.format("%tY", getExpirationDate());
        }

        public String getCallPut() {
            return getKind() == Kind.CALL ? "Call" : "Put";
        }

        public void setSupportsTiedToStock(Boolean supportsTiedToStock) {
            this.supportsTiedToStock = supportsTiedToStock;
        }

        public Boolean getSupportsTiedToStock() {
            return supportsTiedToStock;
        }

        public Mpv getMpv() {
            return mpv;
        }

        public void setMpv(char mpvSymbol) {
            if (Mpv.PennyNickel.tradingSystemIdentifier == mpvSymbol) {
                mpv = Mpv.PennyNickel;
                return;
            }
            if (Mpv.AlwaysPenny.tradingSystemIdentifier == mpvSymbol) {
                mpv = Mpv.AlwaysPenny;
                return;
            }
            if (Mpv.NickelDime.tradingSystemIdentifier == mpvSymbol) {
                mpv = Mpv.NickelDime;
                return;
            }
        }

        public String getPhlxSymbol() {
            return phlxSymbol;
        }

        public void setPhlxSymbol(String phlxSymbol) {
            this.phlxSymbol = phlxSymbol;
        }

        public Character getOptionType() {
            return optionType;
        }

        public void setOptionType(Character optionType) {
            this.optionType = optionType;
        }

        public boolean isNonStandardProduct() {
            return !this.getSupportsTiedToStock();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TestObject strike = (TestObject) o;

            return optionId == strike.optionId;

        }

        @Override
        public int hashCode() {
            return optionId;
        }

    }

    private static class TestObject1 implements Serializable {
        public enum MarketDataChangeType {
            UnderlyingTrade,
            UnderlyingQuote,
            AbboChange,
            PbboChange,
            ComplexTOB,
            ComplexPbbo,
            ComplexNbbo
        }

        public enum OptionQuoteCondition {
            REGULAR,
            NON_FIRM,
            BID_NOT_FIRM,
            OFFER_NOT_FIRM,
            HALTED;
        }

        private MarketDataChangeType type;

        private OptionQuoteCondition condition;

        private Integer optionId;

        private String underlying;

        private byte[] nsk;

        private Integer tradeVolume;

        private Double tradePrice;

        private Double bidPrice;

        private Integer bidSize;

        private Integer customerBidSize;

        private Double askPrice;

        private Integer askSize;

        private Integer customerAskSize;

        private Long timestamp;

        public TestObject1(int i) {
            this.type = MarketDataChangeType.AbboChange;
            this.condition = OptionQuoteCondition.BID_NOT_FIRM;
            this.optionId = i;
            this.underlying = name(i);
            this.nsk = new byte[] {0x00};
            this.tradeVolume = i;
            this.tradePrice = (double) i;
            this.bidPrice = (double) i;
            this.bidSize = i;
            this.customerBidSize = i;
            this.askPrice = (double) i;
            this.askSize = i;
            this.customerAskSize = i;
            this.timestamp = System.currentTimeMillis();
        }


        public MarketDataChangeType getType() {
            return type;
        }

        public OptionQuoteCondition getCondition() {
            return condition;
        }

        public Integer getOptionId() {
            return optionId;
        }

        public String getUnderlying() {
            return underlying;
        }

        public byte[] getNsk() {
            return nsk;
        }

        public Integer getTradeVolume() {
            return tradeVolume;
        }

        public Double getTradePrice() {
            return tradePrice;
        }

        public Double getBidPrice() {
            return bidPrice;
        }

        public Integer getBidSize() {
            return bidSize;
        }

        public Integer getCustomerBidSize() {
            return customerBidSize;
        }

        public Double getAskPrice() {
            return askPrice;
        }

        public Integer getAskSize() {
            return askSize;
        }

        public Integer getCustomerAskSize() {
            return customerAskSize;
        }

        public Long getTimestamp() {
            return timestamp;
        }
    }
}
