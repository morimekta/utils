package net.morimekta.util;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test for the LinkedHashMapBuilder.
 */
public class LinkedHashMapBuilderTest {
    @Test
    public void testBuilder() {
        Map<String, Integer> map = new LinkedHashMapBuilder<String, Integer>()
                .put("a", 44)
                .put("b", 55)
                .put("c", 66)
                .put("d", 77)
                .build();

        List<String> keys = new LinkedList<>(map.keySet());
        // Value order is kept.
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", Strings.asString(keys));

        Map<String, Integer> map2 = new LinkedHashMapBuilder<String, Integer>()
                .putAll(map)
                .build();

        assertEquals(map, map2);
        keys = new LinkedList<>(map2.keySet());
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", Strings.asString(keys));
    }
}
