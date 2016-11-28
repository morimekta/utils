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

import java.util.Objects;

import static org.junit.Assert.assertNotNull;

/**
 * Equality matcher that ignores line indent. But matches all other spacing.
 */
public class IsEqualIgnoreIndent extends BaseMatcher<String> {
    private final String expected;

    public IsEqualIgnoreIndent(String expected) {
        assertNotNull("Missing expected.", expected);

        this.expected = expected;

    }

    @Override
    public boolean matches(Object o) {
        if (o == null || !(CharSequence.class.isAssignableFrom(o.getClass()))) return false;

        String noIndentActual = o.toString().replaceAll("\\r?\\n[ \\t]*", "\n");
        String noIndentExpected = this.expected.replaceAll("\\r?\\n[ \\t]*", "\n");

        return noIndentActual.equals(noIndentExpected);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }

        IsEqualIgnoreIndent other = (IsEqualIgnoreIndent) o;
        return Objects.equals(other.expected, expected);
    }

    @Override
    public int hashCode() {
        return Objects.hash(IsEqualIgnoreIndent.class, expected);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("isEqualIgnoreIndent(" + expected + ")");
    }

    public void describeMismatch(Object actual, Description mismatchDescription) {
        // TODO(show per-line mismatch).
        mismatchDescription.appendText("was " + Objects.toString(actual));
    }
}
