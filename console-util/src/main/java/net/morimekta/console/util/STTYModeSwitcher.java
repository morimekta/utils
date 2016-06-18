package net.morimekta.console.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Switch terminal mode and make it return on close. Basic usage is:
 *
 * <code>
 * try (STTYModeSwitcher tty = new STTYModeSwitcher(STTYMode.RAW)) {
 *     // do stuff in raw mode.
 * }
 * </code>
 */
public class STTYModeSwitcher implements Closeable {
    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @throws IOException If unable to switch.
     */
    public STTYModeSwitcher(STTYMode mode) throws IOException {
        this(mode, Runtime.getRuntime());
    }

    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @param runtime The runtime to execute stty commands on.
     * @throws IOException If unable to switch.

     */
    public STTYModeSwitcher(STTYMode mode, Runtime runtime) throws IOException {
        this.runtime = runtime;
        this.before = setSttyMode(mode);
    }

    /**
     * Close the terminal mode switcher and turn back the the mode before it
     * was opened.
     *
     * @throws IOException If unable to switch back.
     */
    public void close() throws IOException {
        setSttyMode(before);
    }

    // Default input mode is COOKED.
    private static STTYMode current_mode = STTYMode.COOKED;

    private final Runtime  runtime;
    private final STTYMode before;

    /**
     * Set terminal mode.
     *
     * @param mode The mode to set.
     * @return The mode before the call.
     */
    private synchronized STTYMode setSttyMode(STTYMode mode) throws IOException {
        STTYMode old = current_mode;
        if (mode != current_mode) {
            String[] cmd;
            if (mode == STTYMode.COOKED) {
                cmd = new String[]{"/bin/sh", "-c", "stty -raw </dev/tty"};
            } else {
                cmd = new String[]{"/bin/sh", "-c", "stty raw </dev/tty"};
            }

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

            current_mode = mode;
        }
        return old;
    }
}
