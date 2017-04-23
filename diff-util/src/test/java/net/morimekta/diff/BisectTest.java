package net.morimekta.diff;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BisectTest {
    @Test
    public void testBisect() {
        Bisect bisect = new Bisect("abba", "acbca");

        assertThat(bisect.getChangeList(),
                   is(ImmutableList.of(
                           new Change(Operation.EQUAL, "a"),
                           new Change(Operation.INSERT, "c"),
                           new Change(Operation.EQUAL, "b"),
                           new Change(Operation.INSERT, "c"),
                           new Change(Operation.DELETE, "b"),
                           new Change(Operation.EQUAL, "a"))));
        assertThat(bisect.toDelta(), is("=1\t+c\t=1\t+c\t-1\t=1"));
        assertThat(Bisect.fromDelta("abba", bisect.toDelta()),
                   is(bisect));
    }
}
