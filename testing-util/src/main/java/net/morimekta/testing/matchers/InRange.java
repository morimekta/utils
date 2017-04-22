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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Numeric Value range matcher.
 */
public class InRange<T extends Number> extends BaseMatcher<T> {
    private final T lowerInclusive;
    private final T higherExclusive;

    public InRange(T lowerInclusive, T higherExclusive) {
        assertNotNull("Missing lower bound of range.", lowerInclusive);
        assertNotNull("Missing upper bound of range.", higherExclusive);
        assertTrue(String.format("Lower bound %s not lower than upper bound %s of range.", lowerInclusive, higherExclusive),
                   lowerInclusive.doubleValue() < higherExclusive.doubleValue());

        this.lowerInclusive = lowerInclusive;
        this.higherExclusive = higherExclusive;
    }

    @Override
    public boolean matches(Object o) {
        if (o == null || !(Number.class.isAssignableFrom(o.getClass()))) return false;
        Number actual = (Number) o;
        return lowerInclusive.doubleValue() <= actual.doubleValue() &&
               actual.doubleValue() < higherExclusive.doubleValue();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("in range [" + lowerInclusive + " .. " + higherExclusive + ")");
    }

    public void describeMismatch(Object actual, Description mismatchDescription) {
        mismatchDescription.appendText("was ")
                           .appendValue(actual);
    }
}
