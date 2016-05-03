package net.morimekta.util;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test for the LinkedHashMapBuilder.
 */
public class LinkedHashSetBuilderTest {
    @Test
    public void testBuilder() {
        Set<String> set = new LinkedHashSetBuilder<String>()
                .add("a", "b", "c")
                .add("d")
                .build();

        List<String> keys = new LinkedList<>(set);
        // Value order is kept.
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", Strings.asString(keys));

        Set<String> set2 = new LinkedHashSetBuilder<String>()
                .addAll(set)
                .build();

        assertEquals(set, set2);
        keys = new LinkedList<>(set2);
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", Strings.asString(keys));
    }
}
