package net.morimekta.config.source;

import net.morimekta.config.Config;
import net.morimekta.config.ConfigException;
import net.morimekta.config.Sequence;
import net.morimekta.config.format.JsonConfigFormat;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for resource config source.
 */
public class ResourceConfigSourceTest {
    @Test
    public void testLoad() throws IOException, ConfigException {
        ResourceConfigSource source = new ResourceConfigSource("/net/morimekta/config/test.json",
                                                               new JsonConfigFormat());

        Config config = source.load();

        Config expected = Config.builder()
                                .putString("s", "string value.")
                                .putInteger("i", 1234)
                                .putConfig("conf", Config.builder()
                                                         .putString("sub_str", "another string value.")
                                                         .putDouble("real", 1234.5678)
                                                         .build())
                                .putSequence("sequence", Sequence.create(1.0, 2.0, 3.0))
                                .build();

        assertEquals(expected, config);
    }
}
