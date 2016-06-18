package net.morimekta.console;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharReader;
import net.morimekta.console.chr.Unicode;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Terminal interface. It sets proper TTY mode and reads complex characters
 * from the input, and writes lines dependent on terminal mode.
 */
public class Terminal extends CharReader implements Closeable {
    /**
     * LinePrinter interface.
     */
    public interface LinePrinter {
        /**
         * Print a new line to the terminal.
         * @param message The message to write.
         */
        void println(String message);
    }

    /**
     * Construct a default RAW terminal.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal() {
        this(STTYMode.RAW, null);
    }

    /**
     * Construct a terminal with given mode.
     * @param mode The terminal mode.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTYMode mode) {
        this(mode, null);
    }

    /**
     * Construct a terminal with a custom line printer.
     *
     * @param lp The line printer.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(LinePrinter lp) {
        this(STTYMode.RAW, lp);
    }

    /**
     * Construct a terminal with a terminal mode and custom line printer.
     * @param mode The terminal mode.
     * @param lp The line printer.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    public Terminal(STTYMode mode, LinePrinter lp) {
        this(System.in, System.out, mode, lp);
    }

    /**
     * Constructor visible for testing.
     *
     * @param in The input stream.
     * @param out The output stream.
     * @param mode The TTY mode.
     * @param lp The line printer or null.
     * @throws UncheckedIOException If unable to set TTY mode.
     */
    protected Terminal(InputStream in, OutputStream out, STTYMode mode, LinePrinter lp) {
        super(in);
        this.lp = lp == null ? this::printlnInternal : lp;
        this.out = out;
        try {
            this.switcher = new STTYModeSwitcher(mode);
            if (switcher.didChangeMode() && switcher.getBefore() == STTYMode.RAW) {
                this.out.write('\n');
                this.out.flush();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.lineCount = 0;
    }

    /**
     * Make a user confirmation. E.g.:
     *
     * <code>boolean really = term.confirm("Do you o'Really?");</code>
     *
     * Will print out "<code>Do you o'Really? [Y/n]: </code>". If the user press
     * 'y' or 'enter' will pass (return true), if 'n' and 'backspace' will return
     * false.
     *
     * @param what What to confirm. Basically the message before '[Y/n]'.
     * @return Confirmation result.
     */
    public boolean confirm(String what) {
        return confirm(what, true);
    }

    /**
     * Make a user confirmation. E.g.:
     *
     * <code>boolean really = term.confirm("Do you o'Really?", false);</code>
     *
     * Will print out "<code>Do you o'Really? [y/N]: </code>". If the user press
     * 'y' will pass (return true), if 'n', 'enter' and 'backspace' will return
     * false.
     *
     * @param what What to confirm. Basically the message before '[Y/n]'.
     * @param def the default response on 'enter'.
     * @return Confirmation result.
     */
    public boolean confirm(String what, boolean def) {
        String yn = def ? "Y/n" : "y/N";
        formatln("%s [%s]: ", what, yn);

        boolean error = false;

        try {
            for (;;) {
                Char c = read();
                if (c == null) {
                    throw new IOException("End of stream.");
                }
                int cp = c.asInteger();
                if (cp == Char.LF || cp == Char.CR) {
                    return def;
                }
                if (cp == 'y' || cp == 'Y') {
                    return true;
                }
                if (cp == 'n' || cp == 'N' || cp == Char.BS) {
                    return false;
                }
                if (cp == Char.DEL || cp == Char.ABORT || cp == Char.EOF) {
                    throw new IOException("User interrupted");
                }
                if (cp == ' ' || cp == '\t') {
                    continue;
                }
                if (error) {
                    print("        ");
                }
                error = true;
                format("\r%s [%s]: %s is not valid input.",
                       what, yn, c.asString());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Terminal format(String format, Object... args) {
        return print(String.format(format, args));
    }

    public Terminal formatln(String format, Object... args) {
        return println(String.format(format, args));
    }

    public Terminal print(Char ch) {
        return print(ch.toString());
    }

    public Terminal print(String message) {
        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }

    public Terminal println(String message) {
        lp.println(message);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (switcher.didChangeMode() && switcher.getBefore() == STTYMode.COOKED) {
            out.write('\r');
            out.write('\n');
            out.flush();
        }
        switcher.close();
    }

    private void printlnInternal(String message) {
        try {
            if (STTYModeSwitcher.getCurrentMode() == STTYMode.RAW) {
                lnBefore(message);
            } else {
                lnAfter(message);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void lnAfter(String message) throws IOException {
        if (message != null) {
            out.write('\r');
            out.write(message.getBytes(StandardCharsets.UTF_8));
        }
        out.write('\r');
        out.write('\n');
        out.flush();

        ++lineCount;
    }

    private void lnBefore(String message) throws IOException {
        if (lineCount > 0) {
            out.write('\r');
            out.write('\n');
        }
        if (message != null) {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        }
        out.flush();

        ++lineCount;
    }

    private final STTYModeSwitcher switcher;
    private final OutputStream out;
    private final LinePrinter  lp;

    private int lineCount;
}
