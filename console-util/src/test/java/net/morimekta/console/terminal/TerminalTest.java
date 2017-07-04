package net.morimekta.console.terminal;

import net.morimekta.console.chr.Char;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.TerminalSize;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TerminalTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testImpossible() {
        // console.close();  // Ensure we can see output.

        // To gather coverage on methods that cannot work properly both
        // in embedded (non-interactive) unit testing and in a real terminal.
        try (Terminal term = new Terminal()) {
            TerminalSize size = term.getTTY().getTerminalSize();
            System.err.println("Terminal: " + size);
        } catch (Exception e) {
            // silence everything. It is sadly not possible to test the
            // actual output, as it differs between platforms.
            e.printStackTrace(System.err);
        }
    }

    @Test
    public void testForwardedLinePrinter() throws IOException {
        LinePrinter lp = mock(LinePrinter.class);

        try (Terminal term = new Terminal(console.tty(), lp)) {
            term.println("A message");
        }

        verify(lp).println("A message");
        verifyNoMoreInteractions(lp);

        assertThat(console.output(), is(""));
        assertThat(console.error(), is(""));
    }

    @Test
    public void testConfirm() throws IOException {
        assertConfirm("Test", "Test [Y/n]: Yes.", true, 'y');
        assertConfirm("Boo", "Boo [Y/n]: Yes.", true, '\n');
        assertConfirm("Test", "Test [Y/n]: No.", false, 'n');
        assertConfirm("Test", "Test [Y/n]: No.", false, ' ', 'n');

        assertConfirm("Test",
                      "Test [Y/n]:" +
                      "\r\033[KTest [Y/n]: 'g' is not valid input. No.", false, 'g', 'n');

        console.reset();
        console.setInput('\n');

        assertThat(new Terminal(console.tty()).confirm("Test", false), is(false));
        assertThat(console.output(), is("Test [y/N]: No."));

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
    public void testExecuteAbortable() throws IOException, ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();
        try (Terminal term = new Terminal(console.tty())) {
            term.executeAbortable(service, () -> {
                // no-op
            });
            String s = term.executeAbortable(service, () -> "test");
            assertThat(s, is("test"));
        }

        console.reset();
        console.setInput(Char.ESC);

        try (Terminal term = new Terminal(console.tty())) {
            term.executeAbortable(service, () -> {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            fail("No exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Aborted with '<ESC>'"));
        }

        console.reset();
        console.setInput(Char.ABR);

        try (Terminal term = new Terminal(console.tty())) {
            String res = term.executeAbortable(service, () -> {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "fail";
            });
            fail("No exception: " + res);
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Aborted with '<ABR>'"));
        }
    }

    @Test
    public void testExecuteAbortable_aborted() throws IOException, ExecutionException, InterruptedException {
        ExecutorService service = Executors.newSingleThreadExecutor();

        console.setInput(Char.ABR);
        try (Terminal term = new Terminal(console.tty())) {
            term.executeAbortable(service, () -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignore) {
                }
            });
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Aborted with '<ABR>'"));
        }

        console.setInput(Char.ABR);
        try (Terminal term = new Terminal(console.tty())) {
            String s = term.executeAbortable(service, () -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ignore) {
                }
                return "";
            });
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Aborted with '<ABR>'"));
        }
    }

    @Test
    public void testPrinter() throws IOException {
        try (Terminal terminal = new Terminal(console.tty())) {
            new Exception().printStackTrace(terminal.printer());
        }

        assertThat(console.output(),
                   startsWith("java.lang.Exception\tat net.morimekta.console.terminal.TerminalTest.testPrinter(TerminalTest.java:"));
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
