package net.morimekta.config.format;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * Tests for the JSON config format.
 */
public class PropertiesConfigTest {
    private PropertiesConfigParser    parser;
    private PropertiesConfigFormatter formatter;
    private Config                    config;

    @Before
    public void setUp() throws ConfigException {
        JsonConfigParser format = new JsonConfigParser();
        config    = format.parse(getClass().getResourceAsStream("/net/morimekta/config/format/config.json"));
        parser    = new PropertiesConfigParser();
        formatter = new PropertiesConfigFormatter();
    }

    @Test
    public void testFormat() throws IOException, ConfigException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.format(config, baos);

        Properties ps = new Properties();
        ps.load(new ByteArrayInputStream(baos.toByteArray()));

        Properties pf = formatter.format(config);

        assertEquals(pf, ps);

        // NOTE: The ordering here does not really make any sense to me, but it
        // is generated by java... Probably a result of the HashMap. And it's
        // not stable (why??). So sorting all the lines before comparing.
        String[] result = new String(baos.toByteArray(), UTF_8).split("[\\n]");
        // Properties.store even adds the current date...
        result[1] = "# ...(date)...";
        Arrays.sort(result);

        assertEquals("# ...(date)...\n" +
                     "# generated by net.morimekta.config.format.PropertiesConfigFormatter\n" +
                     "b=true\n" +
                     "conf.real=1234.5678\n" +
                     "conf.sub_str=another string value.\n" +
                     "i=1234\n" +
                     "s=string value.\n" +
                     "seq_b.0=false\n" +
                     "seq_b.1=false\n" +
                     "seq_b.2=false\n" +
                     "seq_b.3=true\n" +
                     "seq_i.0=1\n" +
                     "seq_i.1=2.2\n" +
                     "seq_i.2=3.7\n" +
                     "seq_i.3=-4\n" +
                     "seq_s.0=a\n" +
                     "seq_s.1=b\n" +
                     "seq_s.2=c", String.join("\n", (CharSequence[]) result));

        assertEquals("1", pf.getProperty("seq_i.0"));
        assertEquals("another string value.", pf.getProperty("conf.sub_str"));
    }

    @Test
    public void testParse() throws IOException, ConfigException {
        Config cfg = parser.parse(new ByteArrayInputStream(("# ...(date)...\n" +
                                                            "# generated by net.morimekta.config.format.PropertiesConfigFormatter\n" +
                                                            "b=true\n" +
                                                            "conf.real=1234.5678\n" +
                                                            "conf.sub_str=another string value.\n" +
                                                            "i=1234\n" +
                                                            "s=string value.\n" +
                                                            "seq_b.0=false\n" +
                                                            "seq_b.1=false\n" +
                                                            "seq_b.2=false\n" +
                                                            "seq_b.3=true\n" +
                                                            "seq_i.0=1\n" +
                                                            "seq_i.1=2.2\n" +
                                                            "seq_i.2=3.7\n" +
                                                            "seq_i.3=-4\n" +
                                                            "seq_s.0=a\n" +
                                                            "seq_s.1=b\n" +
                                                            "seq_s.2=c").getBytes()));

        assertEquals(config, cfg);
    }
}
