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
package net.morimekta.config.impl;

import net.morimekta.config.Config;
import net.morimekta.config.util.ConfigUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * Immutable configuration object backed by a guava ImmutableMap.
 */
public class ImmutableConfig implements Config {
    /**
     * Create an immutable config instance.
     *
     * @param config The base config (or super-config).
     * @return The immutable config.
     */
    public static ImmutableConfig copyOf(Config config) {
        if (config instanceof ImmutableConfig) {
            return (ImmutableConfig) config;
        } else {
            return new ImmutableConfig(config);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o == null || !(o instanceof Config)) {
            return false;
        }
        return ConfigUtil.equals(this, (Config) o);
    }

    @Override
    public int hashCode() {
        return ConfigUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return ConfigUtil.toString(this);
    }

    @Override
    public Object get(String key) {
        return instance.get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return instance.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return instance.keySet();
    }

    private Object immutable(Object o) {
        if (o instanceof ImmutableList || o instanceof ImmutableSet) {
            return o;
        } else if (o instanceof Set) {
            ImmutableSet.Builder<Object> builder = ImmutableSet.builder();
            for (Object v : (Set) o) {
                builder.add(immutable(v));
            }
            return builder.build();
        } else if (o instanceof Collection) {
            ImmutableList.Builder<Object> builder = ImmutableList.builder();
            for (Object v : (Collection) o) {
                builder.add(immutable(v));
            }
            return builder.build();
        } else {
            // Assume all other types of values are natively immutable.
            return o;
        }
    }

    private ImmutableConfig(Config base) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (String key : base.keySet()) {
            builder.put(key, immutable(base.get(key)));
        }
        instance = builder.build();
    }

    private final ImmutableMap<String, Object> instance;
}
