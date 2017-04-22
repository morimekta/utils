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
package net.morimekta.testing;

import net.morimekta.testing.matchers.AllItemsMatch;
import net.morimekta.testing.matchers.DistinctFrom;
import net.morimekta.testing.matchers.EqualIgnoreIndent;
import net.morimekta.testing.matchers.EqualToLines;
import net.morimekta.testing.matchers.InRange;
import net.morimekta.testing.matchers.MatchesRegex;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Extra hamcrest matchers.
 */
public class ExtraMatchers {
    /**
     * Checks that the value is in a given numeric value range.
     *
     * @param lowerInclusive The lower inclusive accepted value.
     * @param upperExclusive The upper exclusive accepted value (the exact value not accepted).
     * @return The matcher.
     */
    public static <T extends Number> Matcher<T> inRange(@Nonnull T lowerInclusive, @Nonnull T upperExclusive) {
        return new InRange<>(lowerInclusive, upperExclusive);
    }

    /**
     * Equivalent to 'equalTo' for strings that normalize the new-line to '\n',
     * this can make testing that should match output on various platforms
     * easier to wrote.
     *
     * @param expected The expected line content.
     * @return The matcher.
     */
    public static Matcher<String> equalToLines(@Nonnull String expected) {
        return new EqualToLines(expected);
    }

    /**
     * Equivalent to 'equalToLines' but also ignores any indentation each line has.
     *
     * @param expected The expected line content.
     * @return The matcher.
     */
    public static Matcher<String> equalIgnoreIndent(@Nonnull String expected) {
        return new EqualIgnoreIndent(expected);
    }


    /**
     * Alternative to hamcrest 'oneOf' that simplifies to match any one of the
     * <i>values</i> given. And uses a vararg interface
     *
     * @param alternatives The value alternatives.
     * @param <T> The value type.
     * @return The matcher.
     */
    @SafeVarargs
    public static <T> Matcher<T> oneOf(T... alternatives) {
        return Matchers.isOneOf(alternatives);
    }

    /**
     * Matcher to check that two sets does not have any common elements.
     *
     * The elements must have proper equals method implementations for this matcher to work.
     *
     * @param from The set it should not match.
     * @param <T> The element type.
     * @return The distinct-from matcher.
     */
    public static <T> Matcher<Set<T>> distinctFrom(Set<T> from) {
        return new DistinctFrom<>(from);
    }

    /**
     * Matcher that checks all items in a collection to match the item-matcher.
     *
     * @param itemMatcher The matcher for each individual item.
     * @param <T> The item type.
     * @return The collection matcher.
     */
    public static <T> Matcher<Collection<T>> allItemsMatch(Matcher<T> itemMatcher) {
        return new AllItemsMatch<>(itemMatcher);
    }

    /**
     * Matcher that uses a pattern to match against a string.
     *
     * @param pattern The pattern to use.
     * @return The matcher.
     */
    public static Matcher<String> matchesRegex(String pattern) {
        return new MatchesRegex(pattern);
    }

    /**
     * Matcher that uses a pattern to match against a string.
     *
     * @param pattern The pattern to use.
     * @return The matcher.
     */
    public static Matcher<String> matchesRegex(Pattern pattern) {
        return new MatchesRegex(pattern);
    }

    private ExtraMatchers() {}
}
