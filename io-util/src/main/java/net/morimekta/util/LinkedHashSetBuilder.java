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

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Builder for LinkedHashSet similar to that of the guava Immutable sets.
 */
public class LinkedHashSetBuilder<T> {
    private final LinkedHashSet<T> set;

    public LinkedHashSetBuilder() {
        set = new LinkedHashSet<>();
    }

    public LinkedHashSetBuilder<T> add(T first, T... values) {
        set.add(first);
        for (T value : values) {
            set.add(value);
        }
        return this;
    }


    public LinkedHashSetBuilder<T> addAll(Collection<T> collection) {
        set.addAll(collection);
        return this;
    }

    public LinkedHashSet<T> build() {
        return set;
    }
}
