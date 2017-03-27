package net.morimekta.testing.rules;

import net.morimekta.console.Terminal;
import net.morimekta.console.chr.Unicode;
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

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    private final InputStream originalIn;
    private final STTY tty;
    private final InputStream in;

    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream errStream = new ByteArrayOutputStream();
    private ByteArrayInputStream inStream = new ByteArrayInputStream(new byte[0]);

    private PrintStream out;
    private PrintStream err;
    private TerminalSize terminalSize = new TerminalSize(42, 144);

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
                return terminalSize;
            }
        };
    }

    /**
     * Reset stream console.
     */
    public void reset() {
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
        try {
            ByteArrayOutputStream tmp = new ByteArrayOutputStream();
            for (Object c : in) {
                if (c instanceof Character) {
                    tmp.write((Character) c);
                } else if (c instanceof Integer) {
                    // raw unicode codepoint.
                    tmp.write(new Unicode((Integer) c).toString().getBytes(UTF_8));
                } else if (c instanceof Byte) {
                    // raw unicode codepoint (byte value = ASCII).
                    tmp.write(new Unicode((Byte) c).toString().getBytes(UTF_8));
                } else {
                    // Char, String,
                    tmp.write(c.toString().getBytes(UTF_8));
                }
            }
            setInput(tmp.toByteArray());
        } catch (IOException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    /**
     * Set the current terminal size.
     * @param rows Row count.
     * @param cols Column countr.
     * @return The console watcher.
     */
    public ConsoleWatcher setTerminalSize(int rows, int cols) {
        terminalSize = new TerminalSize(rows, cols);
        return this;
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

        System.setIn(in);
        System.setErr(err);
        System.setOut(out);
    }

    @Override
    protected void failed(Throwable e, Description description) {
        close();
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
