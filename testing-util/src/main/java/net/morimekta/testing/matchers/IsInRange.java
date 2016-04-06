package net.morimekta.testing.matchers;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Numeric Value range matcher.
 */
public class IsInRange extends BaseMatcher<Number> {
    private final Number lowerInclusive;
    private final Number higherExclusive;

    public IsInRange(Number lowerInclusive, Number higherExclusive) {
        assertNotNull("Missing lower bound of range.", lowerInclusive);
        assertNotNull("Missing upper bound of range.", higherExclusive);
        assertTrue(String.format("Lower bound %s not lower than upper bound %s of range.", lowerInclusive, higherExclusive),
                   lowerInclusive.doubleValue() < higherExclusive.doubleValue());

        this.lowerInclusive = lowerInclusive;
        this.higherExclusive = higherExclusive;

    }

    @Override
    public boolean matches(Object o) {
        if (o == null || !(o instanceof Number)) return false;
        Number actual = (Number) o;
        return lowerInclusive.doubleValue() <= actual.doubleValue() &&
               actual.doubleValue() < higherExclusive.doubleValue();
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("range(" + lowerInclusive + " <= x < " + higherExclusive + ")");
    }

    public void describeMismatch(Object actual, Description mismatchDescription) {
        mismatchDescription.appendText("was " + actual);
    }
}
