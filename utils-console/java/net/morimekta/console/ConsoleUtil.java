package net.morimekta.console;

/**
 * Common character and console utilities. It can contain both standard unicode characters
 * and unix console control sequences. Here also resides lots of helper
 * methods related to  calculating visible string length, that takes into
 * account control sequences, non-width characters and double width characters.
 */
public class ConsoleUtil {
    /**
     * How many single-characters worth of console real-estate will be taken
     * up by this string if printed. Control characters will be ignored, and
     * double-width characters (CJK) will count as 2 width each.
     *
     * Strings containing carriage movement, CR, LF, unexpanded tabs etc are
     * not allowed, and will cause an IllegalArgumentException.
     *
     * @param string The string to measure.
     * @return The printed width.
     */
    public static int printableWidth(String string) {
        int len = 0;
        char[] cstr = string.toCharArray();
        for (int i = 0; i < cstr.length; ++i) {
            char c = cstr[i];
            if (c == 0x1B) {  // esc

            } else if (c == '\t' || c == '\r' || c == '\n' || c == '\f') {
                throw new IllegalArgumentException("");
            } else if (c < 0x20 || (0x7F <= c && c < 0xA0)) {
                // other control characters. These should have no influence
                // in the printed output, so can safely be ignored.
                throw new IllegalArgumentException("Illegal character in stream 0x" + String.format("%02x", (int) c));
            } else {
                int cp = (int) c;

                // Make sure to consume both surrogates on 32-bit code-points.
                if (Character.isHighSurrogate(c)) {
                    ++i;
                    cp = Character.toCodePoint(c, cstr[i]);
                }

                // Character.isIdeographic(cp) does not seem to return the correct
                // value, i.e it always return false.
                if ((0x00003000 <= cp && cp < 0x00003040) ||  // CJK symbols & punctuations
                    (0x00003300 <= cp && cp < 0x00004DC0) ||  // CJK compatibility (square symbols), Extension A
                    (0x00004E00 <= cp && cp < 0x00010000) ||  // CJK Main group of ideographs
                    (0x00020000 <= cp && cp < 0x0002a6C0) ||  // CJK Extension B
                    (0x0002A700 <= cp && cp < 0x0002CEB0)) {  // CJK Extension C, D, E
                    // CJK or other double-width character.
                    len += 2;
                } else {
                    ++len;
                }
            }
        }
        return len;
    }

    public static String expandTabs(String string) {
        return expandTabs(string, 4);
    }

    public static String expandTabs(String string, int tabWidth) {
        return expandTabs(string, tabWidth, 0);
    }

    public static String expandTabs(String string, int tabWidth, int offset) {
        return string;
    }
}
