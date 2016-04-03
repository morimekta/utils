package net.morimekta.console;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Common character and console utilities. It can contain both standard unicode characters
 * and unix console control sequences. Here also resides lots of helper
 * methods related to  calculating visible string length, that takes into
 * account control sequences, non-width characters and double width characters.
 */
public class ConsoleUtil {
    public static final int TAB_WIDTH = 4;

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
    public static int printableWidth(CharSequence string) {
        AtomicInteger width = new AtomicInteger(0);
        CharStream.stream(string).forEach(c -> width.addAndGet(c.printableWidth()));
        return width.get();
    }

    public static String expandTabs(CharSequence string) {
        return expandTabs(string, TAB_WIDTH);
    }

    public static String expandTabs(CharSequence string, int tabWidth) {
        return expandTabs(string, tabWidth, 0);
    }

    public static String expandTabs(CharSequence string, int tabWidth, int offset) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger off = new AtomicInteger(offset);
        CharStream.stream(string).forEachOrdered(c -> {
            if (c.codepoint() == '\t') {
                int l = tabWidth - (off.get() % tabWidth);
                for (int i = 0; i < l; ++i) {
                    builder.append(' ');
                }
                off.addAndGet(l);
            } else {
                builder.append(c);
                off.addAndGet(c.printableWidth());
            }
        });
        return builder.toString();
    }

    private ConsoleUtil() {}
}
