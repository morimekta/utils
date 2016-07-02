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

/**
 * Base configuration container. Essentially a type-safe map that group
 * values into a few basic types:
 */
public interface ConfigBuilder extends Config {
    /**
     * Put a value into the config.
     *
     * @param key The config to put.
     * @param value The value to put.
     * @return The value replaced if the key was already present.
     */
    Object put(String key, Object value);

    /**
     * Put all values from the 'other' config into this.
     *
     * @param other The other config.
     * @return The config.
     */
    default ConfigBuilder putAll(Config other) {
        for (String key : other.keySet()) {
            put(key, other.get(key));
        }
        return this;
    }

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default ConfigBuilder putBoolean(String key, boolean value) {
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
    default ConfigBuilder putInteger(String key, int value) {
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
    default ConfigBuilder putLong(String key, long value) {
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
    default ConfigBuilder putDouble(String key, double value) {
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
    default ConfigBuilder putString(String key, String value) {
        put(key, value);
        return this;
    }

    /**
     * Put a collection value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default <T> ConfigBuilder putCollection(String key, Collection<T> value) {
        put(key, value);
        return this;
    }

}
