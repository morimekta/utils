package net.morimekta.console.terminal;

import net.morimekta.console.chr.Char;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.util.STTYMode;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TerminalTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testConfirm() throws IOException {
        assertConfirm("Test", "Test [Y/n]: Yes.", true, 'y');
        assertConfirm("Boo", "Boo [Y/n]: Yes.", true, '\n');
        assertConfirm("Test", "Test [Y/n]: No.", false, 'n');
        assertConfirm("Test", "Test [Y/n]: No.", false, ' ', 'n');

        assertConfirm("Test",
                      "Test [Y/n]:" +
                      "\r\033[KTest [Y/n]: 'g' is not valid input. No.", false, 'g', 'n');

        try {
            console.reset();
            console.setInput('\t', Char.ESC);
            new Terminal(console.tty()).confirm("Test");
            fail("no exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(), is("java.io.IOException: User interrupted: <ESC>"));
        }

        try {
            console.reset();
            new Terminal(console.tty()).confirm("Test");
            fail("no exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(), is("java.io.IOException: End of stream."));
        }
    }

    private void assertConfirm(String msg,
                               String out,
                               boolean value,
                               Object... in) {
        console.reset();
        console.setInput(in);

        assertThat(new Terminal(console.tty()).confirm(msg), is(value));
        assertThat(console.output(), is(out));
    }

    @Test
    public void testOpenClose() throws IOException {
        try (Terminal outer = new Terminal(console.tty())) {
            assertThat(console.output(), is(""));
            console.reset();

            try (Terminal inner = new Terminal(console.tty(), STTYMode.COOKED)) {
                assertThat(console.output(), is("\n"));

                inner.println("foo");
                console.reset();
            }

            assertThat(console.output(), is(""));
            outer.println("bar");
            console.reset();
        }

        assertThat(console.output(), is("\r\n"));
    }
}
