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
public class TomlConfigTest {
    private JsonConfigFormatter formatter;
    private TomlConfigParser    parser;

    @Before
    public void setUp() throws ConfigException {
        formatter = new JsonConfigFormatter();
        parser    = new TomlConfigParser();
    }

    @Test
    public void testFormat() throws ConfigException {
        Config config = parser.parse(getClass().getResourceAsStream("/net/morimekta/config/format/config.toml"));

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
}
