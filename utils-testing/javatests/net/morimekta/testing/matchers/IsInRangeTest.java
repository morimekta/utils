package net.morimekta.testing.matchers;

import junit.framework.AssertionFailedError;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing the is-in-range matcher.
 */
public class IsInRangeTest {
    @Test
    public void testConstructor() {
        IsInRange range = new IsInRange(4, 5);
        assertFalse(range.matches(3.9999));
        assertTrue(range.matches(4));
        assertTrue(range.matches(4.9999));
        assertFalse(range.matches(5));

        assertBad("Lower bound 4 not lower than upper bound 4 of range.", 4, 4);
        assertBad("Missing lower bound of range.", null, 4);
        assertBad("Missing upper bound of range.", 4, null);
    }

    private void assertBad(String message, Number lower, Number upper) {
        try {
            new IsInRange(lower, upper);
            fail(String.format("Expected exception on new IsInRange(%s, %s)",
                               Objects.toString(lower),
                               Objects.toString(upper)));
        } catch (AssertionFailedError e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testDescribeTo() {
        StringDescription description = new StringDescription();

        new IsInRange(12, 20).describeTo(description);
        assertEquals("range(12 <= x < 20)", description.toString());

        description = new StringDescription();

        new IsInRange(-123.45, 123.45).describeTo(description);
        assertEquals("range(-123.45 <= x < 123.45)", description.toString());
    }

    @Test
    public void testDescribeMismatch() {
        StringDescription description = new StringDescription();

        new IsInRange(12, 20).describeMismatch(10, description);
        assertEquals("was 10", description.toString());

        description = new StringDescription();

        new IsInRange(-123.45, 123.45).describeMismatch(255.6789, description);
        assertEquals("was 255.6789", description.toString());
    }
}
