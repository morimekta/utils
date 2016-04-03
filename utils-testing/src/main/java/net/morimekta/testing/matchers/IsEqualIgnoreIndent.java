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
    public void describeTo(Description description) {
        description.appendText("isEqualIgnoreIndent(" + expected + ")");
    }

    public void describeMismatch(Object actual, Description mismatchDescription) {
        // TODO(show per-line mismatch).
        mismatchDescription.appendText("was " + Objects.toString(actual));
    }
}
