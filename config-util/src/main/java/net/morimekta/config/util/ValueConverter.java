package net.morimekta.config.util;

/**
 * Simple interface for functions that converts config values.
 */
@FunctionalInterface
public interface ValueConverter<T> {
    T convert(Object o);
}
