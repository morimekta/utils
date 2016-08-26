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

import net.morimekta.config.util.ConfigUtil;
import net.morimekta.config.util.ValueConverter;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static net.morimekta.config.util.ConfigUtil.asBoolean;
import static net.morimekta.config.util.ConfigUtil.asCollection;
import static net.morimekta.config.util.ConfigUtil.asDate;
import static net.morimekta.config.util.ConfigUtil.asDouble;
import static net.morimekta.config.util.ConfigUtil.asInteger;
import static net.morimekta.config.util.ConfigUtil.asLong;
import static net.morimekta.config.util.ConfigUtil.asString;

/**
 * Base configuration container. Essentially a type-safe map.
 *
 * It is not implementing the Map base class since it would require also
 * implementing generic entry adders (put, putAll), and getType unsafe getters.
 */
public interface Config {

    /**
     * Look up a single value from the config.
     *
     * @param key The key to look for.
     * @return The value if found, null otherwise.
     */
    Object get(String key);

    /**
     * Checks if the key exists in the config.
     *
     * @param key The key to look for.
     * @return True if the value exists. False otherwise.
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
     *         requested type.
     */
    default String getString(String key) {
        return asString(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The string value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default String getString(String key, String def) {
        return getWithDefault(key, ConfigUtil::asString, def);
    }

    /**
     * @param key The key to look for.
     * @return The boolean value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default boolean getBoolean(String key) {
        return asBoolean(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The boolean value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default boolean getBoolean(String key, boolean def) {
        return getWithDefault(key, ConfigUtil::asBoolean, def);
    }

    /**
     * @param key The key to look for.
     * @return The integer value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default int getInteger(String key) {
        return asInteger(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The integer value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default int getInteger(String key, int def) {
        return getWithDefault(key, ConfigUtil::asInteger, def);
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
     *         requested type.
     */
    default long getLong(String key, long def) {
        return getWithDefault(key, ConfigUtil::asLong, def);
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
     *         requested type.
     */
    default double getDouble(String key, double def) {
        return getWithDefault(key, ConfigUtil::asDouble, def);
    }

    /**
     * @param key The key to look for.
     * @return The date value.
     * @throws KeyNotFoundException When the key does not exist.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default Date getDate(String key) {
        return asDate(getValue(key));
    }

    /**
     * @param key The key to look for.
     * @param def The default value if not found.
     * @return The date value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested type.
     */
    default Date getDate(String key, Date def) {
        return getWithDefault(key, ConfigUtil::asDate, def);
    }

    /**
     * @param key The key to look for.
     * @param <T> The collection entry type.
     * @return The collection.
     */
    default <T> Collection<T> getCollection(String key) {
        return asCollection(getValue(key));
    }

    /**
     * Get a value from the config looking up deeply into the config. It can also look
     * "up" from the object. The "up" context is always the same for the same config
     * instance. E.g.
     *
     * @param key The key to look up.
     * @param <T> The value type.
     * @return The value.
     * @throws KeyNotFoundException If not found.
     */
    @SuppressWarnings("unchecked")
    default <T> T getValue(String key) {
        if (!containsKey(key)) {
            throw new KeyNotFoundException("No such config entry \"" + key + "\"");
        }
        return (T) get(key);
    }

    /**
     * Look up a single value from the config. If not found return a default
     * value.
     *
     * @param key The key to look for.
     * @param def The default value.
     * @param <T> The value type.
     * @return The value if found, otherwise the default.
     *
     * @deprecated since 0.3.5, use {@link #getWithDefault(String, ValueConverter, Object)}
     */
    @SuppressWarnings("unchecked,unused")
    @Deprecated
    default <T> T getWithDefault(String key, T def) {
        return getWithDefault(key, v -> (T) v, def);
    }

    /**
     * Look up a single value from the config. If not found return a default
     * value. Convert the value using the given converter function.
     *
     * @param key The key to look for.
     * @param def The default value.
     * @param <T> The value type.
     * @return The value if found, otherwise the default.
     */
    default <T> T getWithDefault(String key, ValueConverter<T> convert, T def) {
        if (containsKey(key)) {
            return convert.convert(get(key));
        }
        return def;
    }
}
