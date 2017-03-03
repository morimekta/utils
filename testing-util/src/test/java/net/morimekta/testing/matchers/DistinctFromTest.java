package net.morimekta.testing.matchers;

import com.google.common.collect.ImmutableSet;
import org.hamcrest.StringDescription;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DistinctFromTest {
    @Test
    public void testDistinctFrom() {
        DistinctFrom<Integer> m = new DistinctFrom<>(ImmutableSet.of(1, 2, 3));

        StringDescription description = new StringDescription();
        m.describeTo(description);
        assertThat(description.toString(), is("distinct from [<1>, <2>, <3>]"));

        description = new StringDescription();
        m.describeMismatch(ImmutableSet.of(2, 4, 6), description);
        assertThat(description.toString(), is("both contained [<2>]"));
    }
}
