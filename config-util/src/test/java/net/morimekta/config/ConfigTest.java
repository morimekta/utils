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
        Config config = new Config();

        assertTrue(config.isEmpty());
        assertEquals(0, config.size());
    }

    @Test
    public void testStringValue() throws ConfigException {
        Config builder = new Config();

        assertSame(builder, builder.putString("a", "b"));
        assertEquals("b", builder.getString("a"));

        assertSame(builder, builder.putString("b.c", "d"));
        assertEquals("d", builder.getString("b.c"));
    }

    @Test
    public void testLongValue() throws ConfigException {
        Config builder = new Config();

        assertSame(builder, builder.putLong("a", 1234567890L));
        assertEquals(1234567890L, builder.getLong("a"));

        assertSame(builder, builder.putLong("b.c", 9876543210L));
        assertEquals(9876543210L, builder.getLong("b.c"));
    }
}
