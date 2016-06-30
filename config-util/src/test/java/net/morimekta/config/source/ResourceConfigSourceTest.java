package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.impl.SimpleConfig;
import net.morimekta.config.format.JsonConfigParser;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * Tests for resource config source.
 */
public class ResourceConfigSourceTest {
    @Test
    public void testLoad() throws IOException, ConfigException {
        Supplier<Config> source = new ResourceConfigSupplier("/net/morimekta/config/test.json",
                                                             new JsonConfigParser());

        Config config = source.get();

        Config expected = new SimpleConfig()
                .putString("s", "string value.")
                .putInteger("i", 1234)
                .putSequence("sequence", ImmutableList.of(1.0, 2.0, 3.0))
                .putString("conf.sub_str", "another string value.")
                .putDouble("conf.real", 1234.5678);

        assertEquals(expected, config);
        assertSame(config, source.get());
    }
}
