package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * Tests for the JSON config format.
 */
public class JsonConfigFormatTest {
    private Config config;
    private JsonConfigFormat formatter;

    @Before
    public void setUp() throws ConfigException {
        JsonConfigFormat format = new JsonConfigFormat();
        config = format.parse(getClass().getResourceAsStream("/net/morimekta/config/format/config.json"));
        formatter = new JsonConfigFormat();
    }

    @Test
    public void testFormat() throws ConfigException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.format(baos, config);

        String result = new String(baos.toByteArray(), UTF_8);

        assertEquals("{" +
                     "\"b\":true," +
                     "\"conf\":{\"real\":1234.5678,\"sub_str\":\"another string value.\"}," +
                     "\"i\":1234," +
                     "\"s\":\"string value.\"," +
                     "\"seq_b\":[false,false,false,true]," +
                     "\"seq_c\":[{\"my\":\"sql\"},{\"my\":\"little pony\"}]," +
                     "\"seq_i\":[1,2.2,3.7,-4]," +
                     "\"seq_s\":[\"a\",\"b\",\"c\"]," +
                     "\"seq_seq\":[[1,2,3],[3,2,1]]" +
                     "}", result);
    }
}
