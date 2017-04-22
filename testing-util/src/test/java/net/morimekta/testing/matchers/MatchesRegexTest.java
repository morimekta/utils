package net.morimekta.testing.matchers;

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MatchesRegexTest {
    @Test
    public void testMatches() {
        assertThat(new MatchesRegex("a.*").matches("aa"), is(true));
        assertThat(new MatchesRegex("a.*").matches("baa"), is(false));
        assertThat(new MatchesRegex("a.*b[1-6]").matches("ayb"), is(false));
        assertThat(new MatchesRegex("a.*b[1-6]").matches("ayb5"), is(true));
        assertThat(new MatchesRegex("a.*b").matches(44), is(false));
    }

    @Test
    public void testDescribeTo() {
        Description description = new StringDescription();
        new MatchesRegex("a.*b[1-6]").describeTo(description);

        assertThat(description.toString(), is("matches /a.*b[1-6]/"));
    }

    @Test
    public void testDescribeMismatch() {
        Description description = new StringDescription();
        new MatchesRegex("a.*b[1-6]").describeMismatch("ayb", description);
        assertThat(description.toString(), is("was \"ayb\""));

        description = new StringDescription();
        new MatchesRegex("a.*b[1-6]").describeMismatch(44, description);
        assertThat(description.toString(), is("was not a string: <44>"));
    }
}
