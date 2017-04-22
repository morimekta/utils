package net.morimekta.testing.matchers;

import com.google.common.collect.ImmutableList;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AllItemsMatchTest {
    @Test
    public void testMatches() {
        Matcher<String>             matcher = is("a");
        Matcher<Collection<String>> sut     = new AllItemsMatch<>(matcher);

        assertThat(sut.matches(ImmutableList.of("a", "a")), is(true));
        assertThat(sut.matches(ImmutableList.of("a", "b")), is(false));

        // If no entries, all (none) items match.
        assertThat(sut.matches(ImmutableList.of()), is(true));

        assertThat(sut.matches(null), is(false));
        assertThat(sut.matches("a"), is(false));
    }

    @Test
    public void testDescribeTo() {
        Matcher<String>             matcher = is("a");
        Matcher<Collection<String>> sut     = new AllItemsMatch<>(matcher);

        Description description = new StringDescription();
        sut.describeTo(description);

        assertThat(description.toString(), is("all items match: is \"a\""));
    }


    @Test
    public void testDescribeMismatch() {
        Matcher<String>             matcher = is("a");
        Matcher<Collection<String>> sut     = new AllItemsMatch<>(matcher);

        Description description = new StringDescription();
        sut.describeMismatch("a", description);

        assertThat(description.toString(), is("not a collection: \"a\""));

        description = new StringDescription();
        sut.describeMismatch(ImmutableList.of("a", "b"), description);

        assertThat(description.toString(), is("was [\"a\", \"b\"]"));

    }
}
