package net.morimekta.testing;

import net.morimekta.testing.matchers.IsEqualIgnoreIndent;
import net.morimekta.testing.matchers.IsInRange;

import org.hamcrest.Matcher;

/**
 * Extra hamcrest matchers.
 */
public class ExtraMatchers {
    public static Matcher<Number> isInRange(Number lowerInclusive, Number upperExclusive) {
        return new IsInRange(lowerInclusive, upperExclusive);
    }

    public static Matcher<String> isEqualIgnoreIndent(String expected) {
        return new IsEqualIgnoreIndent(expected);
    }

    private ExtraMatchers() {}
}
