package net.morimekta.console.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Switch terminal mode and make it return on close. Basic usage is:
 *
 * <code>
 * try (new SttyModeSwitcher(SttyMode.RAW)) {
 *     // do stuff in raw mode.
 * }
 * </code>
 */
public class SttyModeSwitcher implements Closeable {
    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @throws IOException If unable to switch.
     */
    public SttyModeSwitcher(SttyMode mode) throws IOException {
        this(mode, Runtime.getRuntime());
    }

    /**
     * Switch to the requested mode until closed.
     *
     * @param mode The mode to switch to.
     * @param runtime The runtime to execute stty commands on.
     * @throws IOException If unable to switch.

     */
    public SttyModeSwitcher(SttyMode mode, Runtime runtime) throws IOException {
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
    private static SttyMode current_mode = SttyMode.COOKED;

    private final Runtime runtime;
    private final SttyMode before;

    /**
     * Set terminal mode.
     *
     * @param mode The mode to set.
     * @return The mode before the call.
     */
    private SttyMode setSttyMode(SttyMode mode) throws IOException {
        SttyMode old = current_mode;
        if (mode != current_mode) {
            if (mode == SttyMode.COOKED) {
                String[] cmd = {"/bin/sh", "-c", "stty -raw </dev/tty"};
                runtime.exec(cmd);
            } else {
                String[] cmd = {"/bin/sh", "-c", "stty raw </dev/tty"};
                runtime.exec(cmd);
            }
        }
        return old;
    }
}
