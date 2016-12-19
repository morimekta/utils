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
package net.morimekta.testing.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;
import java.util.Objects;

/**
 * Numeric Value range matcher.
 */
public class OneOf<T> extends BaseMatcher<T> {
    private final T[] alternatives;

    public OneOf(T... alternatives) {
        assert alternatives.length > 1 : "Must have at least 2 alternatives, got " + alternatives.length;

        boolean hasNull = false;
        for (Object o : alternatives) {
            if (hasNull && o == null) {
                throw new AssertionError("Only one 'null' value allowed as alternative.");
            }
            if (o == null) {
                hasNull = true;
            }
        }

        this.alternatives = alternatives;
    }

    @Override
    public boolean matches(Object o) {
        for (Object a : alternatives) {
            if (a == null && o == null) {
                return true;
            }
            if ((a == null) != (o == null)) {
                continue;
            }
            if (a.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }

        OneOf other = (OneOf) o;
        return Arrays.equals(alternatives, other.alternatives);
    }

    @Override
    public int hashCode() {
        return Objects.hash(OneOf.class, Arrays.hashCode(alternatives));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("one of [");
        boolean first = true;
        for (Object o : alternatives) {
            if (first) {
                first = false;
            } else {
                description.appendText(", ");
            }
            description.appendValue(o);
        }
        description.appendText("]");
    }

    public void describeMismatch(Object actual, Description mismatchDescription) {
        mismatchDescription.appendText("was ");
        mismatchDescription.appendValue(actual);
    }
}
