package net.morimekta.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for LinkedHashMap similar to that of the guava Immutable maps.
 */
public class LinkedHashMapBuilder<K, V> {
    private final LinkedHashMap<K, V> map;

    public LinkedHashMapBuilder() {
        map = new LinkedHashMap<>();
    }

    public LinkedHashMapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public LinkedHashMapBuilder<K, V> putAll(Map<K, V> values) {
        map.putAll(values);
        return this;
    }

    public LinkedHashMap<K, V> build() {
        return map;
    }
}
