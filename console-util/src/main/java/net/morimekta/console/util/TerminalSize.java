package net.morimekta.console.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;

/**
 * Column and row count for the current terminal.
 */
public class TerminalSize {
    public final int cols;
    public final int rows;

    private TerminalSize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    @Override
    public String toString() {
        return String.format("tty(cols:%d, rows:%d)", cols, rows);
    }

    /**
     * Get the terminal size.
     *
     * @return the terminal size.
     * @throws UncheckedIOException If getting the terminal size failed.
     */
    public static TerminalSize get() {
        try {
            return get(Runtime.getRuntime());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get the terminal size.
     *
     * @param runtime The runtime to run the stty command.
     * @return the terminal size.
     * @throws IOException If getting the terminal size failed.
     */
    protected static TerminalSize get(Runtime runtime) throws IOException {
        String[] cmd = new String[]{"/bin/sh", "-c", "stty size </dev/tty"};
        Process p = runtime.exec(cmd);
        try {
            p.waitFor();
        } catch (InterruptedException ie) {
            throw new IOException(ie.getMessage(), ie);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String err = reader.readLine();
            if (err != null) {
                throw new IOException(err);
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String out = reader.readLine();
            if (out != null) {
                String[] parts = out.trim().split("[ ]");
                if (parts.length == 2) {
                    int cols = Integer.parseInt(parts[0]);
                    int rows = Integer.parseInt(parts[1]);
                    return new TerminalSize(cols, rows);
                }
                throw new IOException("Unknown 'stty size' output: " + out);
            }
            throw new IOException("No 'stty size' output.");
        }
    }
}
