package intrino;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(stream, Page.class);
    }
}
