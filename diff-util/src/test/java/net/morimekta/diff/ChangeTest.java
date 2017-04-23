package net.morimekta.diff;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class ChangeTest {
    @Test
    public void testChange() {
        Change a = new Change(Operation.INSERT, "abba");
        Change b = new Change(Operation.INSERT, "abba");
        Change c = new Change(Operation.DELETE, "aqua");

        assertThat(a.asString(), is("+abba"));
        assertThat(a.toString(), is("Change(INSERT,\"abba\")"));
        assertThat(a, is(a));
        assertThat(a, is(b));
        assertThat(a, is(not(c)));
        assertThat(a.hashCode(), is(b.hashCode()));
        assertThat(a.hashCode(), is(not(c.hashCode())));
        assertThat(a.equals(null), is(false));
        assertThat(a.equals(new Object()), is(false));
    }
}
