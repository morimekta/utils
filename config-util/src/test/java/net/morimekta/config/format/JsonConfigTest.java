package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the JSON config format.
 */
public class JsonConfigTest {
    private JsonConfigFormatter formatter;
    private JsonConfigParser    parser;

    @Rule
    public TemporaryFolder temp;

    @Before
    public void setUp() throws IOException {
        temp = new TemporaryFolder();
        temp.create();

        formatter = new JsonConfigFormatter();
        parser    = new JsonConfigParser();
    }

    @Test
    public void testFormat() throws ConfigException {
        Config config = parser.parse(getClass().getResourceAsStream("/net/morimekta/config/format/config.json"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.format(config, baos);

        String result = new String(baos.toByteArray(), UTF_8);

        assertEquals("{" +
                     "\"b\":true," +
                     "\"conf.real\":1234.5678," +
                     "\"conf.sub_str\":\"another string value.\"," +
                     "\"i\":1234," +
                     "\"s\":\"string value.\"," +
                     "\"seq_b\":[false,false,false,true]," +
                     "\"seq_i\":[1,2.2,3.7,-4]," +
                     "\"seq_s\":[\"a\",\"b\",\"c\"]" +
                     "}", result);
    }

    @Test
    public void testEmptyConfig() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream("{}".getBytes(UTF_8));

        Config config = parser.parse(in);

        assertTrue(config.keySet().isEmpty());
    }

    @Test
    public void testEmptyCollection() {
        ByteArrayInputStream in = new ByteArrayInputStream("{\"coll\":[]}".getBytes(UTF_8));

        Config config = parser.parse(in);

        Collection<Object> c = config.getCollection("coll");

        assertNotNull(c);
        assertTrue(c.isEmpty());
    }

    @Test
    public void testConvenience() {
        assertEquals("{" +
                     "\"b\":true," +
                     "\"conf.real\":1234.5678," +
                     "\"conf.sub_str\":\"another string value.\"," +
                     "\"i\":1234," +
                     "\"s\":\"string value.\"," +
                     "\"seq_b\":[false,false,false,true]," +
                     "\"seq_i\":[1,2.2,3.7,-4]," +
                     "\"seq_s\":[\"a\",\"b\",\"c\"]" +
                     "}", formatter.formatToString(
                             parser.parseString("{" +
                                                "\"b\":true," +
                                                "\"conf.real\":1234.5678," +
                                                "\"conf.sub_str\":\"another string value.\"," +
                                                "\"i\":1234," +
                                                "\"s\":\"string value.\"," +
                                                "\"seq_b\":[false,false,false,true]," +
                                                "\"seq_i\":[1,2.2,3.7,-4]," +
                                                "\"seq_s\":[\"a\",\"b\",\"c\"]" +
                                                "}")));
    }
}
