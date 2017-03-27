package net.morimekta.testing.rules;

import net.morimekta.console.Terminal;
import net.morimekta.console.chr.Char;
import net.morimekta.console.util.STTYMode;
import org.junit.Rule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class ConsoleWatcherTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testConsole() {
        System.out.println("out");
        System.err.println("err");

        assertThat(console.output(), is("out\n"));
        assertThat(console.error(), is("err\n"));
    }

    @Test
    public void testInput() throws IOException {
        console.setInput("in!\n");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        assertThat(reader.readLine(), is("in!"));

        console.setInput("out!\n");

        assertThat(reader.readLine(), is("out!"));
    }

    @Test
    public void testInput_unicode() throws IOException {
        console.setInput(Char.DEL, "foo", 3677);

        assertThat(System.in.read(), is(127));  // del
        assertThat(System.in.read(), is(102));  // f
        assertThat(System.in.read(), is(111));  // o
        assertThat(System.in.read(), is(111));  // o
        assertThat(System.in.read(), is(224));  // unicode 3677 byte[0]
        assertThat(System.in.read(), is(185));  // unicode 3677 byte[1]
        assertThat(System.in.read(), is(157));  // unicode 3677 byte[2]
        assertThat(System.in.read(), is(-1));
    }

    @Test
    public void testTerminal() {
        Terminal terminal = console.terminal(STTYMode.RAW);

        console.setInput("y");

        assertThat(terminal.confirm("Do ya?"), is(true));
        assertThat(console.output(), is("Do ya? [Y/n]: Yes."));

        terminal.finish();

        assertThat(console.output(), is("Do ya? [Y/n]: Yes.\r\n"));

        console.reset();

        assertThat(console.output(), is(""));
    }

    @Test
    public void testTTY() {
        Terminal terminal = console.terminal(STTYMode.COOKED);

        assertThat(terminal.getTTY(), sameInstance(console.tty()));
        assertThat(terminal.getTTY().getTerminalSize().rows, is(42));
        assertThat(terminal.getTTY().getTerminalSize().cols, is(144));

        console.setTerminalSize(55, 123);
        assertThat(terminal.getTTY().getTerminalSize().rows, is(55));
        assertThat(terminal.getTTY().getTerminalSize().cols, is(123));
    }
}
