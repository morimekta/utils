package net.morimekta.testing.matchers;

import org.hamcrest.StringDescription;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing the is-equal-ignore-indent matcher.
 */
public class EqualIgnoreIndentTest {
    @Test
    public void testConstructor() {
        EqualIgnoreIndent range = new EqualIgnoreIndent("short\n\t\tabba");
        assertFalse(range.matches("blah"));
        assertTrue(range.matches("short\nabba"));
        assertTrue(range.matches("short\n        abba"));
        assertFalse(range.matches("short abba"));

        assertBad("Missing expected.", null);
    }

    private void assertBad(String message, String str) {
        try {
            new EqualIgnoreIndent(str);
            fail(String.format("Expected exception on new EqualIgnoreIndent(%s)",
                               Objects.toString(str)));
        } catch (AssertionError e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testDescribeTo() {
        StringDescription description = new StringDescription();

        new EqualIgnoreIndent("short\n\t\tabba").describeTo(description);
        assertEquals("equalIgnoreIndent(short\n" +
                     "\t\tabba)", description.toString());
    }

    @Test
    public void testDescribeMismatch() {
        StringDescription description = new StringDescription();

        new EqualIgnoreIndent("short\n\t\tabba").describeMismatch("short\n\n  abba", description);
        assertEquals("was short\n" +
                     "\n" +
                     "  abba", description.toString());
    }
}
