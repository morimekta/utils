package net.morimekta.console;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Class that handled reading a line from terminal input with
 * character and line validators, and optional tab completion.
 */
public class LineInput {
    public interface LineValidator {
        boolean validate(String line, LinePrinter errorPrinter);
    }

    public interface CharValidator {
        boolean validate(Char ch, LinePrinter errorPrinter);
    }

    /**
     * Tab completion interface.
     */
    public interface TabCompletion {
        /**
         * Try to complete the given string.
         *
         * @param before The string to be completed. What is before the carriage.
         * @param errorPrinter Print errors or alternatives to this line printer.
         * @return the completed string, or null if no completion.
         */
        String complete(String before, LinePrinter errorPrinter);
    }


    public LineInput(Terminal terminal,
                     String message,
                     CharValidator charValidator,
                     LineValidator lineValidator,
                     TabCompletion tabCompletion) {
        if (charValidator == null) {
            charValidator = (c, o) -> {
                int ch = c.asInteger();
                // Just check for printable ASCII character
                if (ch < 0x20 || 0x7f <= ch) {
                    o.println("Invalid character: " + c.asString());
                    return false;
                }
                return true;
            };
        }
        if (lineValidator == null) {
            lineValidator = (l, o) -> {
                // Accept all non-empty lines.
                if (l.length() == 0) {
                    o.println("Output needs at least 1 character.");
                    return false;
                }
                return true;
            };
        }

        this.terminal = terminal;
        this.message = message;
        this.charValidator = charValidator;
        this.lineValidator = lineValidator;
        this.tabCompletion = tabCompletion;
    }

    public String readLine() {
        return readLine(null);
    }

    public String readLine(String initial) {
        this.before = initial == null ? "" : initial;
        this.after = "";
        this.printedError = null;

        if (initial != null && initial.length() > 0 &&
                !lineValidator.validate(initial, e -> {})) {
            throw new IllegalArgumentException("Invalid initial value: " + initial);
        }

        terminal.formatln("%s: %s", message, before);

        try {
            for (; ; ) {
                Char c = terminal.read();
                if (c == null) {
                    throw new IOException("End of input.");
                }

                int ch = c.asInteger();
                if (ch == Char.TAB && tabCompletion != null) {
                    String completed = tabCompletion.complete(before, this::printAbove);
                    if (completed != null) {
                        before = completed;
                        printInputLine();
                    }
                    continue;
                }

                if (ch == Char.CR) {
                    String line = before + after;
                    if (lineValidator.validate(line, this::printAbove)) {
                        return line;
                    }
                    continue;
                }

                if (ch == Char.DEL) {
                    // backspace...
                    before = before.substring(0, before.length() - 1);
                    printInputLine();
                    continue;
                }

                if (c instanceof Control) {
                    if (c.equals(Control.DELETE)) {
                        if (after.length() > 0) {
                            after = after.substring(1);
                        }
                    } else if (c.equals(Control.LEFT)) {
                        if (before.length() > 0) {
                            after = "" + before.charAt(before.length() - 1) + after;
                            before = before.substring(0, before.length() - 1);
                        }
                    } else if (c.equals(Control.CTRL_LEFT)) {
                        after = before + after;
                        before = "";
                    } else if (c.equals(Control.RIGHT)) {
                        if (after.length() > 0) {
                            before = before + after.charAt(0);
                            after = after.substring(1);
                        }
                    } else if (c.equals(Control.CTRL_RIGHT)) {
                        before = before + after;
                        after = "";
                    } else {
                        printAbove("Invalid control: " + c.asString());
                        continue;
                    }
                    printInputLine();
                    continue;
                }

                if (ch == Char.ESC || ch == Char.ABR || ch == Char.EOF) {
                    throw new IOException("User interrupted: " + c.asString());
                }

                if (charValidator.validate(c, this::printAbove)) {
                    before = before + c.toString();
                    printInputLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void printAbove(String error) {
        if (printedError != null) {
            terminal.format("\r%s%s",
                            Control.UP,
                            Control.CURSOR_ERASE);
        } else {
            terminal.format("\r%s", Control.CURSOR_ERASE);
        }
        printedError = error;
        terminal.print(error);
        terminal.println();
        printInputLine();
    }

    private void printInputLine() {
        terminal.format("\r%s%s: %s",
                        Control.CURSOR_ERASE,
                        message,
                        before);
        if (after.length() > 0) {
            terminal.format("%s%s", after, Control.cursorLeft(after.length()));
        }
    }

    private final Terminal terminal;
    private final String message;
    private final CharValidator charValidator;
    private final LineValidator lineValidator;
    private final TabCompletion tabCompletion;

    private String before;
    private String after;
    private String printedError;
}
