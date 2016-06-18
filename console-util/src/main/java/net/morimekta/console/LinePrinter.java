package net.morimekta.console;

import net.morimekta.console.chr.Color;

/**
 * LinePrinter interface.
 */
public interface LinePrinter {
    /**
     * Print a new line to the terminal.
     * @param message The message to write.
     */
    void println(String message);

    /**
     * Print an info string message.
     *
     * @param message The info message.
     */
    default void info(String message) {
        println(String.format("%s[info]%s %s",
                              Color.GREEN,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print a warning string message.
     *
     * @param message The warning message.
     */
    default void warn(String message) {
        println(String.format("%s[warn]%s %s",
                              Color.YELLOW,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print an error string message.
     *
     * @param message The error message.
     */
    default void error(String message) {
        println(String.format("%s[error]%s %s",
                              Color.RED,
                              Color.CLEAR,
                              message));
    }

    /**
     * Print a fatal string message.
     *
     * @param message The fatal message.
     */
    default void fatal(String message) {
        println(String.format("%s[FATAL]%s %s",
                              new Color(Color.RED, Color.BOLD),
                              Color.CLEAR,
                              message));
    }
}
