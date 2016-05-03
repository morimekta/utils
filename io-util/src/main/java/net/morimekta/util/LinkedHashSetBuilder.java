package net.morimekta.util;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Builder for LinkedHashSet similar to that of the guava Immutable sets.
 */
public class LinkedHashSetBuilder<T> {
    private final LinkedHashSet<T> set;

    public LinkedHashSetBuilder() {
        set = new LinkedHashSet<>();
    }

    public LinkedHashSetBuilder<T> add(T first, T... values) {
        set.add(first);
        for (T value : values) {
            set.add(value);
        }
        return this;
    }


    public LinkedHashSetBuilder<T> addAll(Collection<T> collection) {
        set.addAll(collection);
        return this;
    }

    public LinkedHashSet<T> build() {
        return set;
    }
}
