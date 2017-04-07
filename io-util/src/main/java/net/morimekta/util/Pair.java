/*
 * Copyright (c) 2016, Stein Eldar johnsen
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
package net.morimekta.util;

import java.util.Objects;

/**
 * Container to ease passing around a tuple of two objects. This object
 * provides a sensible implementation of equals(), returning true if equals()
 * is true on each of the contained objects.
 */
public class Pair<F, S> {
    /**
     * The first value.
     */
    public final F first;

    /**
     * The second value.
     */
    public final S second;

    /**
     * Constructor for a Pair.
     *
     * @param first First part of pair.
     * @param second Second part of pair.
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Convenience method for creating an appropriately typed pair.
     *
     * @param <A> First type.
     * @param <B> Second type.
     * @param a First part.
     * @param b Second part.
     * @return The resulting pair.
     */
    public static <A, B> Pair<A, B> create(A a, B b) {
        return new Pair<>(a, b);
    }

    /**
     * Convenience method to get the first value.
     *
     * @return The first value.
     */
    public F getFirst() {
        return first;
    }

    /**
     * Convenience method to get the second value.
     *
     * @return The second value.
     */
    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
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
