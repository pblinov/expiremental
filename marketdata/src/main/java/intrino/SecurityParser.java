package intrino;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import marketdata.transport.Quote;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * @author pblinov
 * @since 15/09/2017
 */
public class SecurityParser {
    public static Page parse(InputStream stream) throws IOException {
        ObjectMapper mapper = getMapper();
        return mapper.readValue(stream, Page.class);
    }

    public static Quote parse(String message) throws IOException {
        ObjectMapper mapper = getMapper();
        return mapper.readValue(message, Quote.class);
    }

    private static ObjectMapper getMapper() {
        return new ObjectMapper();
    }

    public static String toJson(Quote quote) throws JsonProcessingException {
        return getMapper().writeValueAsString(quote);
    }
}
