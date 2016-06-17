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
package net.morimekta.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for LinkedHashMap similar to that of the guava Immutable maps.
 */
public class LinkedHashMapBuilder<K, V> {
    private final LinkedHashMap<K, V> map;

    public LinkedHashMapBuilder() {
        map = new LinkedHashMap<>();
    }

    public LinkedHashMapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public LinkedHashMapBuilder<K, V> putAll(Map<K, V> values) {
        map.putAll(values);
        return this;
    }

    public LinkedHashMap<K, V> build() {
        return map;
    }
}
