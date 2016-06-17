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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Mutable configuration object. It does not enforce type mutability, and will
 * keep a reference to the base config if provided.
 *
 * NOTE: Changed in values on the base config does *not* propagate to the
 * mutable config after it's creation. The map is mutable as it is there to be
 * used for manipulating the config during parsing or generation.
 */
public class MutableConfig extends Config implements ConfigBuilder<MutableConfig> {
    /**
     * Create an empty config instance.
     */
    public MutableConfig() {
        this(null, null);
    }

    /**
     * Create an empty config instance with parent.
     *
     * @param parent The parent (parent) config.
     */
    public MutableConfig(MutableConfig parent) {
        this(parent, null);
    }

    /**
     * Create an empty config instance with parent and base.
     *
     * @param parent The parent (parent) config.
     * @param base The base config (or super-config).
     */
    public MutableConfig(MutableConfig parent, Config base) {
        this.parent = parent;
        this.base = base;

        this.map = new TreeMap<>();
        if (base != null) {
            for (Entry entry : base.entrySet()) {
                this.map.put(entry.getKey(), entry);
            }
        }
    }

    /**
     * Get the parent config used for 'up' navigation.
     * @return The parent config.
     */
    public Config getParent() {
        return parent;
    }

    /**
     * Get the base config used for 'super' navigation.
     * @return The base config.
     */
    public Config getBase() {
        return base;
    }

    /**
     * Get a mutable config value. If a config does not exists for the given
     * key, one is created. If a value that is not a config exists for the key
     * an exception is thrown.
     *
     * @param key The recursive key to look parent.
     * @return The config value.
     * @throws IncompatibleValueException When a value cannot be converted to
     *         requested getType.
     */
    public MutableConfig mutableConfig(String key) {
        MutableConfig cfg;
        if (map.containsKey(key)) {
            Config existing = map.get(key).asConfig();
            if (existing instanceof MutableConfig) {
                cfg = (MutableConfig) existing;
                if (cfg.getBase() != this) {
                    cfg = new MutableConfig(this, existing);
                    putConfig(key, cfg);
                }
            } else {
                cfg = new MutableConfig(this, existing);
                putConfig(key, cfg);
            }
        } else {
            cfg = new MutableConfig(this);
            putConfig(key, cfg);
        }
        return cfg;
    }

    /**
     * Put a value into the config.
     *
     * @param key The simple key to put at.
     * @param value The value to put.
     * @return The config.
     */
    public MutableConfig putValue(String key, Value value) {
        map.put(key, new ImmutableEntry(key, value));
        return this;
    }

    /**
     * Remove entry with the given key.
     *
     * @param key The key to remove.
     * @return The config.
     */
    public MutableConfig remove(String key) {
        map.remove(key);
        return this;
    }

    /**
     * Clear the config.
     *
     * @return The config.
     */
    public MutableConfig clear() {
        map.clear();
        return this;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Set<Entry> entrySet() {
        return new TreeSet<>(map.values());
    }

    @Override
    public Value getValue(String key) {
        if (!map.containsKey(key)) {
            throw new KeyNotFoundException("No such key " + key);
        }
        return map.get(key).getValue();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    // --- private

    private final Map<String, Entry> map;
    private final MutableConfig parent;
    private final Config base;

}
