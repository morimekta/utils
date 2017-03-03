package net.morimekta.testing.matchers;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class EqualToLinesTest {
    @Test
    public void testEqualToLines() {
        String unixText =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                "sed do eiusmod tempor incididunt ut labore et dolore magna\n" +
                "aliqua. Ut enim ad minim veniam, quis nostrud exercitation\n" +
                "ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis\n" +
                "aute irure dolor in reprehenderit in voluptate velit esse\n" +
                "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat\n" +
                "cupidatat non proident, sunt in culpa qui officia deserunt\n" +
                "mollit anim id est laborum.";
        String windowsText = unixText.replaceAll("\\n", "\r\n");

        String diffWindows = windowsText.replaceAll("cons", "kont");
        String diffUnix = unixText.replaceAll("cons", "kont");

        Matcher<String> windowsMatcher = new EqualToLines(windowsText);
        Matcher<String> unixMatcher = new EqualToLines(unixText);

        assertTrue(windowsMatcher.matches(unixText));
        assertTrue(windowsMatcher.matches(windowsText));
        assertTrue(unixMatcher.matches(unixText));
        assertTrue(unixMatcher.matches(windowsText));

        assertFalse(windowsMatcher.matches(diffUnix));
        assertFalse(windowsMatcher.matches(diffWindows));
        assertFalse(unixMatcher.matches(diffUnix));
        assertFalse(unixMatcher.matches(diffWindows));

        assertThat(describeTo(windowsMatcher), is(equalTo(
                "equal to lines:" + System.lineSeparator() +
                unixText.replaceAll("\\n", System.lineSeparator()))));
        assertThat(describeTo(unixMatcher), is(equalTo(
                "equal to lines:" + System.lineSeparator() +
                unixText.replaceAll("\\n", System.lineSeparator()))));

        assertThat(unixText, is(windowsMatcher));
        assertThat(windowsText, is(unixMatcher));

        assertThat(describeMismatch(unixMatcher, diffWindows), is(equalTo(
                ("has line-by-line diff:\n" +
                "- Lorem ipsum dolor sit amet, consectetur adipiscing elit,\n" +
                "+ Lorem ipsum dolor sit amet, kontectetur adipiscing elit,\n" +
                "  sed do eiusmod tempor incididunt ut labore et dolore magna\n" +
                "  aliqua. Ut enim ad minim veniam, quis nostrud exercitation\n" +
                "- ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis\n" +
                "+ ullamco laboris nisi ut aliquip ex ea commodo kontequat. Duis\n" +
                "  aute irure dolor in reprehenderit in voluptate velit esse\n" +
                "  cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat\n" +
                "  cupidatat non proident, sunt in culpa qui officia deserunt\n" +
                "  mollit anim id est laborum.\n").replaceAll("\\n", System.lineSeparator()))));
    }

    private String describeTo(Matcher<?> matcher) {
        StringDescription description = new StringDescription();
        matcher.describeTo(description);
        return description.toString();
    }

    private String describeMismatch(Matcher<?> matcher, Object o) {
        StringDescription description = new StringDescription();
        matcher.describeMismatch(o, description);
        return description.toString();
    }

}
