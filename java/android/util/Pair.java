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

package android.util;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object
 * provides a sensible implementation of equals(), returning true if equals()
 * is true on each of the contained objects.
 */
public final class Pair<F, S> {
    public final F first;
    public final S second;

    /**
     * Constructor for a Pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) o;

        return (Objects.equals(first, other.first) &&
                Objects.equals(second, other.second));
    }

    @Override
    public int hashCode() {
        return Objects.hash(Pair.class, first, second);
    }

    @Override
    public String toString() {
        return String.format("(%s,%s)",
                             Objects.toString(first),
                             Objects.toString(second));
    }
}
