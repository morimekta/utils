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
 * Testing the is-equal-ignore-indent matcher.
 */
public class IsEqualIgnoreIndentTest {
    @Test
    public void testConstructor() {
        IsEqualIgnoreIndent range = new IsEqualIgnoreIndent("short\n\t\tabba");
        assertFalse(range.matches("blah"));
        assertTrue(range.matches("short\nabba"));
        assertTrue(range.matches("short\n        abba"));
        assertFalse(range.matches("short abba"));

        assertBad("Missing expected.", null);
    }

    private void assertBad(String message, String str) {
        try {
            new IsEqualIgnoreIndent(str);
            fail(String.format("Expected exception on new IsEqualIgnoreIndent(%s)",
                               Objects.toString(str)));
        } catch (AssertionError e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testDescribeTo() {
        StringDescription description = new StringDescription();

        new IsEqualIgnoreIndent("short\n\t\tabba").describeTo(description);
        assertEquals("isEqualIgnoreIndent(short\n" +
                     "\t\tabba)", description.toString());
    }

    @Test
    public void testDescribeMismatch() {
        StringDescription description = new StringDescription();

        new IsEqualIgnoreIndent("short\n\t\tabba").describeMismatch("short\n\n  abba", description);
        assertEquals("was short\n" +
                     "\n" +
                     "  abba", description.toString());
    }
}
