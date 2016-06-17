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

/**
 * Mutable configuration object. It does not enforce type mutability, and will
 * keep a reference to the base config if provided.
 *
 * NOTE: Changed in values on the base config does *not* propagate to the
 * mutable config after it's creation. The map is mutable as it is there to be
 * used for manipulating the config during parsing or generation.
 */
public interface ConfigBuilder<B extends ConfigBuilder> {
    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    B putValue(String key, Value value);

    /**
     * Remove entry with the given key.
     *
     * @param key The key to remove.
     * @return The config.
     */
    B remove(String key);

    /**
     * Clear the config.
     *
     * @return The config.
     */
    B clear();

    /**
     * Put a boolean value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putBoolean(String key, boolean value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put an integer value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putInteger(String key, int value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a long value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putLong(String key, long value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a double value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putDouble(String key, double value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a string value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putString(String key, String value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a sequence value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putSequence(String key, Sequence value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }

    /**
     * Put a config value into the config.
     *
     * @param key The key to put at.
     * @param value The value to put.
     * @return The config.
     */
    @SuppressWarnings("unchecked")
    default B putConfig(String key, Config value) {
        putValue(key, ImmutableValue.create(value));
        return (B) this;
    }
}
