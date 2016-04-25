package net.morimekta.console.chr;

import net.morimekta.util.Strings;

/**
 *
 * https://en.wikipedia.org/wiki/C0_and_C1_control_codes
 */
public class Control implements Char {
    public static final Control UP    = new Control("\033[A");
    public static final Control DOWN  = new Control("\033[B");
    public static final Control RIGHT = new Control("\033[C");
    public static final Control LEFT  = new Control("\033[D");

    public static final Control CTRL_UP    = new Control("\033[1;5A");
    public static final Control CTRL_DOWN  = new Control("\033[1;5B");
    public static final Control CTRL_RIGHT = new Control("\033[1;5C");
    public static final Control CTRL_LEFT  = new Control("\033[1;5D");

    public static final Control CURSOR_ERASE   = new Control("\033[K");
    public static final Control CURSOR_SAVE    = new Control("\033[s");
    public static final Control CURSOR_RESTORE = new Control("\033[u");

    public static final Control DPAD_MID = new Control("\033[E");

    public static final Control INSERT    = new Control("\033[2~");
    public static final Control DELETE    = new Control("\033[3~");
    public static final Control HOME      = new Control("\033[1~");
    public static final Control END       = new Control("\033[4~");
    public static final Control PAGE_UP   = new Control("\033[5~");
    public static final Control PAGE_DOWN = new Control("\033[6~");

    public static final Control F1 = new Control("\033OP");
    public static final Control F2 = new Control("\033OQ");
    public static final Control F3 = new Control("\033OR");
    public static final Control F4 = new Control("\033OS");
    public static final Control F5 = new Control("\033[15~");
    public static final Control F6 = new Control("\033[17~");
    public static final Control F7 = new Control("\033[18~");
    public static final Control F8 = new Control("\033[19~");
    public static final Control F9 = new Control("\033[20~");

    private final String str;

    public Control(CharSequence str) {
        this.str = str.toString();
    }

    @Override
    public int asInteger() {
        return -1;
    }

    @Override
    public String asString() {
        return Strings.escape(str);
    }

    @Override
    public int printableWidth() {
        return 0;
    }

    @Override
    public int length() {
        return str.length();
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof Control)) {
            return false;
        }
        Control other = (Control) o;

        return str.equals(other.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }
}
