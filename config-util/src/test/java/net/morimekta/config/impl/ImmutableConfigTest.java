package net.morimekta.config.impl;

import net.morimekta.config.Config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

/**
 * Tests for the immutable config.
 */
public class ImmutableConfigTest {
    @Test
    public void testCopyOf() {
        SimpleConfig simple = new SimpleConfig();
        simple.putString("a", "false");

        ImmutableConfig i1 = ImmutableConfig.copyOf(simple);
        ImmutableConfig i2 = ImmutableConfig.copyOf(i1);

        assertSame(i1, i2);
        assertEquals("false", i2.getString("a"));
    }

    @Test
    public void testToString() {
        SimpleConfig builder = new SimpleConfig();
        builder.putString("a", "false");
        ImmutableConfig config = ImmutableConfig.copyOf(builder);

        assertEquals("ImmutableConfig{a=false}", config.toString());
    }

    @Test
    public void testEquals() {
        SimpleConfig builder = new SimpleConfig();
        builder.putString("a", "false");
        ImmutableConfig config = ImmutableConfig.copyOf(builder);

        assertEquals(config, config);
        assertNotEquals(config, null);
        assertNotEquals(config, "4");

        Config cfg = config;
        assertEquals(cfg, builder);
    }
}
