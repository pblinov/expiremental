package intrino;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author pblinov
 * @since 15/09/2017
 */
class SecurityParserTest {
    @Test
    void parseSingleItem() throws IOException {
        InputStream stream = getClass().getResourceAsStream("security.json");
        assertNotNull(stream);
        Security security = SecurityParser.parse(stream);
        assertNotNull(security);
        assertEquals("MSFT", security.ticker);
    }

    @Test
    void parseMultipleItems() throws IOException {
        InputStream stream = getClass().getResourceAsStream("securities.json");
        assertNotNull(stream);
        Page page = SecurityParser.parse(stream);
        assertNotNull(page);
        assertNotNull(page.data);
        assertEquals(100, page.data.size());
        page.data.stream().forEach(item-> System.out.println(item.stock_exchange));
    }

}