package net.morimekta.console.util;

import net.morimekta.console.Terminal;

import java.util.ArrayList;
import java.util.Collection;

import static net.morimekta.console.chr.CharUtil.printableWidth;
import static net.morimekta.console.chr.Control.CURSOR_ERASE;
import static net.morimekta.console.chr.Control.UP;
import static net.morimekta.console.chr.Control.cursorDown;
import static net.morimekta.console.chr.Control.cursorRight;
import static net.morimekta.console.chr.Control.cursorUp;

/**
 * Class that holds a set of buffer, that are printed to the terminal, and
 * methods to dynamically update those buffer. It will keep the cursor at
 * the bottom line (end of printed line) for easy continuation.
 */
public class TerminalBuffer {
    private final Terminal          terminal;
    private final ArrayList<String> buffer;

    public TerminalBuffer(Terminal terminal) {
        this.terminal = terminal;
        this.buffer = new ArrayList<>();
    }

    public int count() {
        return buffer.size();
    }

    public void add(String ... lines) {
        for (String line : lines) {
            buffer.add(line);
            terminal.println(line);
        }
    }

    public void add(Collection<String> lines) {
        for (String line : lines) {
            buffer.add(line);
            terminal.println(line);
        }
    }

    /**
     * Update a specific line.
     * @param i The line index.
     * @param line The new line content.
     */
    public void update(int i, String line) {
        if (i >= count()) {
            throw new IndexOutOfBoundsException("Index: " + i + ", Size: " + count());
        }
        String old = buffer.get(i);
        if (old.equals(line)) {
            // No change.
            return;
        }
        buffer.set(i, line);

        int up = count() - i - 1;

        terminal.print("\r");
        if (up > 0) {
            terminal.print(cursorUp(up));
        }
        terminal.print(CURSOR_ERASE);
        terminal.print(line);

        // Move the cursor back to the end of the last line.
        if (up > 0) {
            terminal.format("\r%s%s",
                            cursorDown(up),
                            cursorRight(printableWidth(lastLine())));
        }
    }

    /**
     * Clear the entire buffer.
     */
    public void clear() {
        if (buffer.size() > 0) {
            terminal.format("\r%s", CURSOR_ERASE);
            for (int i = 1; i < buffer.size(); ++i) {
                terminal.format("%s%s", UP, CURSOR_ERASE);
            }
            buffer.clear();
        }
    }

    /**
     * Clear the last N lines.
     *
     * @param N Number of lines to clear.
     */
    public void clearLast(int N) {
        if (N < 1) {
            throw new IllegalArgumentException("Unable to clear " + N + " lines");
        }
        if (N > count()) {
            throw new IllegalArgumentException("Count: " + N + ", Size: " + count());
        }
        if (N == count()) {
            clear();
            return;
        }

        terminal.format("\r%s", CURSOR_ERASE);
        buffer.remove(buffer.size() - 1);
        for (int i = 1; i < N; ++i) {
            terminal.format("%s%s", UP, CURSOR_ERASE);
            buffer.remove(buffer.size() - 1);
        }

        if (count() > 0) {
            terminal.format("\r%s%s",
                            UP,
                            cursorRight(printableWidth(lastLine())));
        }
    }

    private String lastLine() {
        return buffer.get(buffer.size() - 1);
    }
}
