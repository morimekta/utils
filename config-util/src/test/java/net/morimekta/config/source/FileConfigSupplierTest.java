package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.format.JsonConfigParser;
import net.morimekta.util.io.IOUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the file source for config.
 */
public class FileConfigSupplierTest {
    @Rule
    public TemporaryFolder tmp;

    private File             cfg;

    @Before
    public void setUp() throws IOException {
        tmp = new TemporaryFolder();
        tmp.create();

        cfg = tmp.newFile("config.json");
        try (FileOutputStream out = new FileOutputStream(cfg);
             InputStream in = getClass().getResourceAsStream("/net/morimekta/config/test.json")) {
            IOUtils.copy(in, out);
            out.flush();
        }
    }

    @Test
    public void testLoad() throws IOException, ConfigException, InterruptedException {
        FileConfigSupplier src = new FileConfigSupplier(cfg);
        Config config = src.get();

        assertEquals("string value.", config.getString("s"));
        assertEquals(1234, config.getLong("i"));
        assertEquals("another string value.", config.getValue("conf.sub_str"));
        assertEquals(1234.5678, config.getDouble("conf.real"), 0.001);

        final long[] array = new long[3];
        AtomicInteger pos = new AtomicInteger(0);
        config.getCollection("sequence").stream().forEachOrdered(l -> {
            int i = pos.getAndIncrement();
            array[i] = ((Number) l).longValue();
        });
        assertArrayEquals(new long[]{1, 2, 3}, array);

        assertSame(config, src.get());

        // update file to test2.json
        try (FileOutputStream out = new FileOutputStream(cfg);
             InputStream in = getClass().getResourceAsStream("/net/morimekta/config/test2.json")) {
            IOUtils.copy(in, out);
            out.flush();
        }

        assertSame(config, src.get());

        src.reload();

        Config config_2 = src.get();
        assertNotSame(config, config_2);

        assertFalse(config_2.containsKey("as"));
        assertTrue(config_2.containsKey("conf.real"));
    }
}
