package net.morimekta.testing.rules;

import net.morimekta.console.Terminal;
import net.morimekta.console.chr.Char;
import net.morimekta.console.util.STTYMode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

import static net.morimekta.console.chr.CharUtil.stripNonPrintable;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ConsoleWatcherTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher()
            .withTerminalSize(20, 80);

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
    public void testTerminal_tty() throws IOException {
        console.setInput('y', 'n');

        try (Terminal terminal = new Terminal(console.tty())) {
            assertThat(terminal.confirm("Do ya?"), is(true));
            assertThat(terminal.confirm("O'Really?"), is(false));
        }

        assertThat(console.output(),
                   is("Do ya? [Y/n]: Yes.\r\n" +
                      "O'Really? [Y/n]: No."));
    }

    @Test
    public void testTerminal_nonInteractive() throws IOException {
        console.setInput('y')
               .nonInteractive();

        try (Terminal terminal = new Terminal(console.tty())) {
            assertThat(terminal.getTTY().isInteractive(), is(false));

            try {
                terminal.getTTY().getTerminalSize();
                fail("no exception");
            } catch (UncheckedIOException e) {
                assertThat(e.getMessage(), is("java.io.IOException: Non-interactive test-terminal"));
            }
        }
    }

    @Test
    public void testFailed() {
        Description description = Description.createTestDescription(ConsoleWatcherTest.class, "testFailed");
        ConsoleWatcher inner = new ConsoleWatcher();
        inner.dumpOnFailure();
        inner.starting(description);

        System.err.println("ERROR: an error");
        System.out.println("OUT: an output");

        inner.failed(new IOException(), description);
        inner.finished(description);

        assertThat(stripNonPrintable(console.error()),
                   is(" <<< --- stdout : testFailed --- >>>\n" +
                      "OUT: an output\n" +
                      " <<< --- stdout : END --- >>>\n" +
                      "\n" +
                      " <<< --- stderr : testFailed --- >>>\n" +
                      "ERROR: an error\n" +
                      " <<< --- stderr : END --- >>>\n"));
        assertThat(console.output(), is(""));
    }

    @Test
    public void testTTY() {
        Terminal terminal = console.terminal(STTYMode.COOKED);

        assertThat(terminal.getTTY(), sameInstance(console.tty()));
        assertThat(terminal.getTTY().getTerminalSize().rows, is(20));
        assertThat(terminal.getTTY().getTerminalSize().cols, is(80));

        console.withTerminalSize(55, 123);
        assertThat(terminal.getTTY().getTerminalSize().rows, is(55));
        assertThat(terminal.getTTY().getTerminalSize().cols, is(123));
    }
}
