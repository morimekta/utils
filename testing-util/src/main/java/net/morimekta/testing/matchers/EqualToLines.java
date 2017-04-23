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

import net.morimekta.diff.DiffLines;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Equality matcher that that ignores changes in line separators, and shows
 * a line by line diff on mismatch.
 */
public class EqualToLines extends BaseMatcher<String> {
    private final String expected;

    public EqualToLines(String expected) {
        // replace all '\r\n' with '\n' so windows & *nix line output is normalized to the same.
        this.expected = normalize(expected);
    }

    @Override
    public boolean matches(Object item) {
        if (item == null || !(CharSequence.class.isAssignableFrom(item.getClass()))) return false;

        // replace all '\r\n' with '\n' so windows & *nix line output is normalized to the same.
        return expected.equals(normalize(item));
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("equal to lines:" + System.lineSeparator())
                   .appendText(expected.replaceAll("\\n", System.lineSeparator()));
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        if (item == null || !(CharSequence.class.isAssignableFrom(item.getClass()))) {
            super.describeMismatch(item, description);
            return;
        }

        DiffLines diff = new DiffLines(expected, normalize(item));

        description.appendText("has line-by-line diff:" + System.lineSeparator())
                   .appendText(diff.fullDiff().replaceAll("\n", System.lineSeparator()));
    }

    private static String normalize(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString().replaceAll("\\r\\n", "\n");
    }
}
