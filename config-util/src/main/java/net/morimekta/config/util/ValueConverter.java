package net.morimekta.config.util;

/**
 * Simple interface for functions that converts config values.
 */
@FunctionalInterface
public interface ValueConverter<T> {
    /**
     * Convert value to a given type.
     *
     * @param o Object to convert.
     * @return The converted instance.
     */
    T convert(Object o);
}
