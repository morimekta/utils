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

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeSet;

/**
 * Immutable config container. It enforces deep immutability of all values,
 * and does not keep track of "parent", "base" or similar relations. It is
 * simply a config map.
 */
public class ImmutableConfig extends Config {
    /**
     * Create an empty config instance. Handy for testing.
     */
    public ImmutableConfig() {
        map = ImmutableMap.of();
    }

    private ImmutableConfig(Collection<Entry> entries) {
        ImmutableMap.Builder<String, Entry> builder = ImmutableMap.builder();
        entries.forEach(e -> builder.put(e.getKey(), e));
        map = builder.build();
    }

    /**
     * Make a copy of the other config. If the other config is an ImmutableConfig
     * the same instance will be returned.
     *
     * @param base The base config
     * @return The immutable copy.
     */
    public static Config copyOf(Config base) {
        if (base instanceof ImmutableConfig) {
            return base;
        } else {
            return new ImmutableConfig.Builder(base).build();
        }
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(map.keySet());
    }

    @Override
    public Set<Entry> entrySet() {
        return new TreeSet<>(map.values());
    }

    @Override
    protected int spliteratorCapabilities() {
        return Spliterator.IMMUTABLE | super.spliteratorCapabilities();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Value getValue(String key) throws KeyNotFoundException{
        if (!map.containsKey(key)) {
            throw new KeyNotFoundException("No such key " + key);
        }
        return map.get(key).getValue();
    }

    public Builder mutate() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements ConfigBuilder<Builder> {
        private final Map<String, Entry> map;

        public Builder() {
            map = new HashMap<>();
        }

        public Builder(Config base) {
            map = new HashMap<>();
            base.entrySet().forEach(e -> putValue(e.getKey(), e.getValue()));
        }

        public Builder putValue(String key, Value e) {
            switch (e.getType()) {
                case CONFIG:
                    map.put(key,
                            new ImmutableEntry(key,
                                               ImmutableValue.create(copyOf(e.asConfig()))));
                    break;
                case SEQUENCE:
                    map.put(key,
                            new ImmutableEntry(key,
                                               ImmutableValue.create(ImmutableSequence.copyOf(e.asSequence()))));
                    break;
                default:
                    map.put(key,
                            new ImmutableEntry(key,
                                               ImmutableValue.copyOf(e)));
                    break;
            }
            return this;
        }

        public Builder remove(String key) {
            map.remove(key);
            return this;
        }

        public Builder clear() {
            map.clear();
            return this;
        }

        public ImmutableConfig build() {
            return new ImmutableConfig(map.values());
        }
    }

    // --- PRIVATE ---

    private final Map<String, Entry> map;
}
