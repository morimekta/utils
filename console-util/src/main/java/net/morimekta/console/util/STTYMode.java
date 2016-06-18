package net.morimekta.console.util;

/**
 * Which IO mode the TTY is in.
 */
public enum STTYMode {
    /**
     * Raw mode only moves the cursor according to the print and control
     * characters given.
     */
    RAW,

    /**
     * Cooked mode wraps output lines and returns to start of line after
     * newline (essentially an LF automatically makes a CR)
     */
    COOKED
}
