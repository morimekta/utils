/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
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

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A config mapped onto different keys.
 */
public class MappedConfig implements Config {
    private final ImmutableMap<String, String> mapping;
    private final Supplier<Config> contained;

    public MappedConfig(Supplier<Config> contained, Map<String, String> mapping) {
        this.contained = contained;
        this.mapping = ImmutableMap.copyOf(mapping);
    }

    @Override
    public Object get(String key) {
        if (!mapping.containsKey(key)) {
            return null;
        }
        return contained.get().get(mapping.get(key));
    }

    @Override
    public boolean containsKey(String key) {
        if (!mapping.containsKey(key)) {
            return false;
        }
        return contained.get().containsKey(mapping.get(key));
    }

    @Override
    public Set<String> keySet() {
        return mapping.keySet()
                      .stream()
                      .filter(this::containsKey)
                      .collect(Collectors.toSet());
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
}
