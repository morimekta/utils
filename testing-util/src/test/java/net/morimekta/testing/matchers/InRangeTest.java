package net.morimekta.testing.matchers;

import org.hamcrest.StringDescription;
import org.junit.Test;

import java.util.Objects;

import static net.morimekta.testing.ExtraMatchers.inRange;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Testing the is-in-range matcher.
 */
public class InRangeTest {
    @Test
    public void testConstructor() {
        InRange<Integer> range = new InRange<>(4, 5);
        assertThat(range.matches(3.9999), is(false));
        assertThat(range.matches(4),      is(true));
        assertThat(range.matches(4.9999), is(true));
        assertThat(range.matches(5),      is(false));

        assertBad("Lower bound 4 not lower than upper bound 4 of range.", 4, 4);
        assertBad("Missing lower bound of range.", null, 4);
        assertBad("Missing upper bound of range.", 4, null);
    }

    private void assertBad(String message, Number lower, Number upper) {
        try {
            new InRange<>(lower, upper);
            fail(String.format("Expected exception on new InRange(%s, %s)",
                               Objects.toString(lower),
                               Objects.toString(upper)));
        } catch (AssertionError|IllegalArgumentException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testDescribeTo() {
        StringDescription description = new StringDescription();

        new InRange<>(12, 20).describeTo(description);
        assertEquals("in range [12 .. 20)", description.toString());

        description = new StringDescription();

        new InRange<>(-123.45, 123.45).describeTo(description);
        assertEquals("in range [-123.45 .. 123.45)", description.toString());
    }

    @Test
    public void testDescribeMismatch() {
        StringDescription description = new StringDescription();

        new InRange<>(12, 20).describeMismatch(10, description);
        assertEquals("was <10>", description.toString());

        description = new StringDescription();

        new InRange<>(-123.45, 123.45).describeMismatch(255.6789, description);
        assertEquals("was <255.6789>", description.toString());
    }

    @Test
    public void testHelperConstructor() {
        assertThat(5, is(inRange(4, 6)));
        assertThat(5000L, is(inRange(4, 60000)));
        assertThat(5, is(inRange(4L, 60000)));
    }
}
