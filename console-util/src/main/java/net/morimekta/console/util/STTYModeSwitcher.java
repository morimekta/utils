package net.morimekta.console.util;

import java.io.Closeable;
import java.io.IOException;

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
            if (mode == STTYMode.COOKED) {
                String[] cmd = {"/bin/sh", "-c", "stty -raw </dev/tty"};
                runtime.exec(cmd);
            } else {
                String[] cmd = {"/bin/sh", "-c", "stty raw </dev/tty"};
                runtime.exec(cmd);
            }
            current_mode = mode;
        }
        return old;
    }
}
