package net.morimekta.console;

/**
 * Unicode character representation. It represent the full 31-but unicode code
 * point, and will expand to the 2 surrogate paired string if necessary.
 */
public class Unicode implements Char {
    private final int cp;

    public Unicode(int cp) {
        this.cp = cp;
    }

    @Override
    public int codepoint() {
        return cp;
    }

    @Override
    public int printableWidth() {
        // Control characters.
        if (cp < 0x20 ||
            (0x7F <= cp && cp < 0xA0) ||
            cp == 0x061C ||
            (0x2000 <= cp && cp <= 0x202F) ||
            (0xFFF9 <= cp && cp <= 0xFFFB)) {
            return 0;
        }

        // TODO: There is also a set of non-width glyphs, e.g. used in some indian languages.
        // These also have a printable width of 0.

        // Character.isIdeographic(cp) does not seem to return the correct
        // value, i.e it always return false.
        if ((0x00003000 <= cp && cp < 0x00003040) ||  // CJK symbols & punctuations
            (0x00003300 <= cp && cp < 0x00004DC0) ||  // CJK compatibility (square symbols), Extension A
            (0x00004E00 <= cp && cp < 0x00010000) ||  // CJK Main group of ideographs
            (0x00020000 <= cp && cp < 0x0002A6C0) ||  // CJK Extension B
            (0x0002A700 <= cp && cp < 0x0002CEB0)) {  // CJK Extension C, D, E
            // CJK or other double-width character.
            return 2;
        }
        return 1;
    }

    @Override
    public int length() {
        if (!Character.isBmpCodePoint(cp)) {
            return 2;
        }
        return 1;
    }

    @Override
    public String toString() {
        if (!Character.isBmpCodePoint(cp)) {
            return new String(new char[]{
                    Character.highSurrogate(cp),
                    Character.lowSurrogate(cp)
            });
        }
        return new String(new char[]{(char) cp});
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(cp);
    }

    /**
     * Gets the unicode char representing a circled number.
     * @param num
     * @return
     */
    public static Unicode numeric(int num) {
        if (num > 0) {
            if (num <= 20) {
                return new Unicode(0x2460 - 1 + num);
            }
            if (num <= 35) {
                return new Unicode(0x3250 - 20 + num);
            }
            if (num <= 50) {
                return new Unicode(0x32b0 - 35 + num);
            }
        }
        return null;
    }

}
