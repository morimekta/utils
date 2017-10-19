package net.morimekta.config.impl;

import net.morimekta.config.Config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the mapped config.
 */
public class MappedConfigTest {
    @Test
    public void testMappedConfig() {
        Config simple = new SimpleConfig()
                .putInteger("type", 5)
                .putCollection("seq", ImmutableList.of("a", "b"));

        MappedConfig mapped = new MappedConfig(() -> simple, ImmutableMap.of(
                "not.just.seq", "seq",
                "not.found", "not"
        ));

        assertEquals(ImmutableSet.of("not.just.seq"), mapped.keySet());
        assertEquals(ImmutableList.of("a", "b"), mapped.getCollection("not.just.seq"));
        assertFalse(mapped.containsKey("not.found"));
        assertFalse(mapped.containsKey("really.not.found"));
        assertNull(mapped.get("not.found"));
        assertNull(mapped.get("really.not.found"));

        Config other = new SimpleConfig()
                .putCollection("not.just.seq", ImmutableList.of("a", "b"));

        assertTrue(mapped.equals(other));
        assertFalse(mapped.equals(simple));
        assertEquals(mapped, mapped);
        assertNotEquals(mapped, null);
        assertNotEquals(mapped, new ArrayList<>());

        assertEquals("MappedConfig{not.just.seq=[a, b]}", mapped.toString());
    }
}
