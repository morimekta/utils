/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.config;

import java.util.Collection;
import java.util.Set;

import static net.morimekta.config.Value.asBoolean;
import static net.morimekta.config.Value.asCollection;
import static net.morimekta.config.Value.asDouble;
import static net.morimekta.config.Value.asInteger;
import static net.morimekta.config.Value.asLong;
import static net.morimekta.config.Value.asString;

/**
 * Base configuration container. Essentially a type-safe map that group
 * values into a few basic types:
 *
 * <ul>
 *     <li>NUMBER: integer, longs and doubles</li>
 *     <li>STRING: literal string values</li>
 *     <li>BOOLEAN: boolean true or false</li>
 *     <li>SEQUENCE: a sequence of values from this list</li>
 * </ul>
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and getType unsafe getters.
 */
public interface Config {
    /**
     * Look up a single value from the config.
     */
    Object get(String key);

    /**
     * Put a value into the config.
     *
     * @param key The config to put.
     * @param value The value to put.
     * @return The value replaced if the key was already present.
     */
    Object put(String key, Object value);

    /**
     * Checks if the key prefix exists deeply in the config. Also supports 'up'
     * and 'super' navigation, unless the config instance also contains the key
     * "up" or "super".
     *
     * @param key The prefix to look for.
     * @return The
     */
    boolean containsKey(String key);

    /**
     * Get the set of keys available in the config.
     *
     * @return The key set.
     */
    Set<String> keySet();

    /**
     * @param key The key to look for.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default String getString(String key) {
        return asString(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default String getString(String key, String def) {
        if (containsKey(key)) {
            return asString(getValue(key));
        }
        return def;
    }

    /**
     * @param key The key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default boolean getBoolean(String key) {
        return asBoolean(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default boolean getBoolean(String key, boolean def) {
        if (containsKey(key)) {
            return asBoolean(getValue(key));
        }
        return def;
    }

    /**
     * @param key The key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default int getInteger(String key) {
        return asInteger(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default int getInteger(String key, int def) {
        if (containsKey(key)) {
            return asInteger(getValue(key));
        }
        return def;
    }

    /**
     * @param key The key to look for.
     * @return The long value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default long getLong(String key) {
        return asLong(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The long value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default long getLong(String key, long def) {
        if (containsKey(key)) {
            return asLong(getValue(key));
        }
        return def;
    }

    /**
     * @param key The key to look for.
     * @return The double value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default double getDouble(String key) {
        return asDouble(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The double value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    default double getDouble(String key, double def) {
        if (containsKey(key)) {
            return asDouble(getValue(key));
        }
        return def;
    }

    /**
     * @param key The key to look for.
     * @param <T> The collection entry type.
     * @return The collection.
     */
    default <T> Collection<T> getSequence(String key) {
        return asCollection(getValue(key));
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance. E.g.
     *
     * @param key The key to look up.
     * @return The value.
     * @throws KeyNotFoundException If not found.
     */
    @SuppressWarnings("unchecked")
    default <T> T getValue(String key) {
        if (!containsKey(key)) {
            throw new ConfigException("No such key " + key);
        }
        return (T) get(key);
    }

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Config putBoolean(String key, boolean value) {
        put(key, value);
        return this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Config putInteger(String key, int value) {
        put(key, value);
        return this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Config putLong(String key, long value) {
        put(key, value);
        return this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Config putDouble(String key, double value) {
        put(key, value);
        return this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Config putString(String key, String value) {
        put(key, value);
        return this;
    }

    /**
     * Put a sequence value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default <T> Config putSequence(String key, Collection<T> value) {
        put(key, value);
        return this;
    }

}
