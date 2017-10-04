package marketdata;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import marketdata.transport.Quote;

/**
 * @author pblinov
 * @since 04/10/2017
 */
public class QuoteSerializer {
    private static final int BUFFER_SIZE = 1024;
    private static final int QUOTE_CLASS_ID = 100;

    private Kryo kryo;

    public QuoteSerializer() {
        kryo = new Kryo();
        kryo.register(Quote.class, QUOTE_CLASS_ID);
    }

    public synchronized byte[] serialize(Quote quote) {
        Output output = new Output(BUFFER_SIZE);
        kryo.writeObject(output, quote);
        byte[] result = output.toBytes();
        output.close();
        return result;
    }

    public synchronized Quote deserialize(byte[] data) {
        Input input = new Input(data);
        Quote quote = kryo.readObject(input, Quote.class);
        input.close();
        return quote;
    }
}
