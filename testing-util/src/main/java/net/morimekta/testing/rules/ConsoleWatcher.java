package net.morimekta.testing.rules;

import net.morimekta.console.Terminal;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.chr.Color;
import net.morimekta.console.util.STTY;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import net.morimekta.console.util.TerminalSize;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Printed output watcher rule.
 *
 * <pre>
 * public class Test {
 *     &amp;Rule
 *     public ConsoleWatcher out = new ConsoleWatcher();
 *
 *     &amp;Test
 *     public void testOutput() {
 *         System.err.println("woot!");
 *
 *         assertThat(out.error(), is("woot!\n"));
 *     }
 * }
 * </pre>
 */
public class ConsoleWatcher extends TestWatcher implements AutoCloseable {
    private static final TerminalSize DEFAULT_TERMINAL_SIZE = new TerminalSize(42, 144);

    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final InputStream originalIn;
    private final STTY tty;
    private final InputStream in;

    private ByteArrayOutputStream outStream = null;
    private ByteArrayOutputStream errStream = null;
    private ByteArrayInputStream inStream = null;
    private PrintStream out;

    private PrintStream err;
    private TerminalSize terminalSize = DEFAULT_TERMINAL_SIZE;
    private TerminalSize defaultTerminalSize = DEFAULT_TERMINAL_SIZE;
    private boolean interactive = true;
    private boolean defaultInteractive = true;
    private boolean dumpOutputOnFailure = false;
    private boolean dumpErrorOnFailure = false;
    private boolean defaultDumpOutputOnFailure = false;
    private boolean defaultDumpErrorOnFailure = false;
    private boolean started = false;

    public ConsoleWatcher() {
        originalErr = System.err;
        originalOut = System.out;
        originalIn = System.in;

        in = new WrappedInputStream();

        tty = new STTY() {
            @Override
            public STTYModeSwitcher setSTTYMode(STTYMode mode) {
                try {
                    return makeSttyModeSwitcher(mode);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public TerminalSize getTerminalSize() {
                if (!isInteractive()) {
                    throw new UncheckedIOException(new IOException("Non-interactive test-terminal"));
                }
                return terminalSize;
            }

            @Override
            public boolean isInteractive() {
                return interactive;
            }
        };
    }

    /**
     * Set the current terminal size.
     *
     * @param rows Row count.
     * @param cols Column count.
     * @return The console watcher.
     */
    public ConsoleWatcher withTerminalSize(int rows, int cols) {
        terminalSize = new TerminalSize(rows, cols);
        if (!started) {
            defaultTerminalSize = terminalSize;
        }
        return this;
    }

    /**
     * Set input mode to non-interactive. This makes the terminal no longer
     * behave like an interactive terminal (the default for ConsoleWatcher),
     * but as a wrapped shell script.
     *
     * @return The console watcher.
     */
    public ConsoleWatcher nonInteractive() {
        interactive = false;
        if (!started) {
            defaultInteractive = false;
        }
        return this;
    }

    /**
     * Set input mode to interactive. This makes the terminal behave like an
     * interactive terminal (the default for ConsoleWatcher).
     *
     * @return The console watcher.
     */
    public ConsoleWatcher interactive() {
        interactive = true;
        if (!started) {
            defaultInteractive = true;
        }
        return this;
    }


    /**
     * Dump stdout to error output on failure.
     *
     * @return The console watcher.
     */
    public ConsoleWatcher dumpOutputOnFailure() {
        dumpOutputOnFailure = true;
        if (!started) {
            defaultDumpOutputOnFailure = true;
        }
        return this;
    }

    /**
     * Dump stderr to error output on failure.
     *
     * @return The console watcher.
     */
    public ConsoleWatcher dumpErrorOnFailure() {
        dumpErrorOnFailure = true;
        if (!started) {
            defaultDumpErrorOnFailure = true;
        }
        return this;
    }

    /**
     * Dump both stdout and stderr to error output on failure.
     *
     * @return The console watcher.
     */
    public ConsoleWatcher dumpOnFailure() {
        dumpOutputOnFailure();
        dumpErrorOnFailure();
        return this;
    }

    /**
     * Reset stream console.
     */
    public void reset() {
        if (!started) {
            throw new AssertionError("Resetting a non-started console watcher.");
        }
        setUpStreams();

        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }

    /**
     * @return Get the normal output.
     */
    public String output() {
        return new String(outStream.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * @return Get the error output.
     */
    public String error() {
        return new String(errStream.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Set input with dynamic content.
     * @param in The input values.
     */
    public void setInput(Object... in) {
        assert in.length > 0 : "Require at least one input item";
        setInput(CharUtil.inputBytes(in));
    }

    /**
     * Set input to return the given string content.
     * @param in The string input.
     */
    public void setInput(@Nonnull String in) {
        setInput(in.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Set input to return the given bytes.
     * @param in The bytes for input.
     */
    public void setInput(@Nonnull byte[] in) {
        assert started : "Setting input on a non-started console";
        inStream = new ByteArrayInputStream(in);
    }

    /**
     * @return The testing TTY
     */
    public STTY tty() {
        return tty;
    }

    /**
     * Make a terminal for testing.
     * @param mode The output mode of the terminal.
     * @return The testing terminal
     */
    public Terminal terminal(STTYMode mode) {
        try {
            return new Terminal(tty,
                                in,
                                outStream,
                                null,
                                makeSttyModeSwitcher(mode));
        } catch (IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        System.setErr(originalErr);
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Override
    protected void starting(Description description) {
        setUpStreams();

        started = true;
        interactive = defaultInteractive;
        terminalSize = defaultTerminalSize;
        dumpErrorOnFailure = defaultDumpErrorOnFailure;
        dumpOutputOnFailure = defaultDumpOutputOnFailure;

        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (dumpOutputOnFailure) {
            originalErr.println(Color.BOLD + " <<< --- stdout : " + description.getMethodName() + " --- >>> " + Color.CLEAR);
            originalErr.print(output());
            originalErr.println(Color.BOLD + " <<< --- stdout : END --- >>> " + Color.CLEAR);
            if (dumpErrorOnFailure) {
                originalErr.println();
            }
        }
        if (dumpErrorOnFailure) {
            originalErr.println(Color.BOLD + " <<< --- stderr : " + description.getMethodName() + " --- >>> " + Color.CLEAR);
            originalErr.print(error());
            originalErr.println(Color.BOLD + " <<< --- stderr : END --- >>> " + Color.CLEAR);
        }
    }

    @Override
    protected void finished(Description description) {
        close();
    }

    private void setUpStreams() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        inStream = new ByteArrayInputStream(new byte[0]);

        out = new PrintStream(outStream);
        err = new PrintStream(errStream);
    }

    private STTYModeSwitcher makeSttyModeSwitcher(STTYMode mode) throws IOException {
        return new STTYModeSwitcher(mode, Runtime.getRuntime()) {
            @Override
            protected void setSttyMode(STTYMode mode) throws IOException {
                // Do nothing.
            }
        };
    }

    private class WrappedInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return inStream.read();
        }

        @Override
        public int read(@Nonnull byte[] bytes) throws IOException {
            return inStream.read(bytes);
        }

        @Override
        public int read(@Nonnull byte[] bytes, int i, int i1) throws IOException {
            return inStream.read(bytes, i, i1);
        }

        @Override
        public boolean markSupported() {
            return inStream.markSupported();
        }

        @Override
        public long skip(long l) throws IOException {
            return inStream.skip(l);
        }

        @Override
        public void mark(int i) {
            inStream.mark(i);
        }

        @Override
        public void reset() throws IOException {
            inStream.reset();
        }

        @Override
        public void close() throws IOException {
            inStream.close();
        }
    }
}
