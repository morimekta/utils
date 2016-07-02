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
import java.util.Date;

/**
 * Base configuration container. Essentially a type-safe map that group
 * values into a few basic types:
 */
public interface ConfigBuilder<Builder extends ConfigBuilder<Builder>> extends Config {
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
    @SuppressWarnings("unchecked")
    default Builder putAll(Config other) {
        for (String key : other.keySet()) {
            put(key, other.get(key));
        }
        return (Builder) this;
    }

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putBoolean(String key, boolean value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putInteger(String key, int value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putLong(String key, long value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putDouble(String key, double value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putString(String key, String value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put a date value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default Builder putDate(String key, Date value) {
        put(key, value);
        return (Builder) this;
    }

    /**
     * Put a collection value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default <T> Builder putCollection(String key, Collection<T> value) {
        put(key, value);
        return (Builder) this;
    }

}
