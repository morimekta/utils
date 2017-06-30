package net.morimekta.console.util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TerminalSizeTest {
    @Test
    public void testTerminalSize() {
        TerminalSize term = new TerminalSize(12, 34);
        TerminalSize same = new TerminalSize(12, 34);
        TerminalSize other = new TerminalSize(23, 45);

        assertThat(term.rows, is(12));

        assertThat(term, is(term));
        assertThat(term, is(same));
        assertThat(term, is(not(other)));
        assertThat(term.equals(null), is(false));
        assertThat(term.equals(new Object()), is(false));
    }
}
