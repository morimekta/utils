package net.morimekta.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ConfigTest {
    @Test
    public void testConfig() {
        Config config = Config.builder().build();

        assertTrue(config.isEmpty());
        assertEquals(0, config.size());
    }

    @Test
    public void testStringValue() throws ConfigException {
        Config.Builder builder = new Config.Builder();

        assertSame(builder, builder.putString("a", "b"));
        assertEquals("b", builder.build().getString("a"));

        assertSame(builder, builder.putString("b.c", "d"));
        assertEquals("d", builder.build().getString("b.c"));
    }

    @Test
    public void testLongValue() throws ConfigException {
        Config.Builder builder = new Config.Builder();

        assertSame(builder, builder.putLong("a", 1234567890L));
        assertEquals(1234567890L, builder.build().getLong("a"));

        assertSame(builder, builder.putLong("b.c", 9876543210L));
        assertEquals(9876543210L, builder.build().getLong("b.c"));
    }
}
