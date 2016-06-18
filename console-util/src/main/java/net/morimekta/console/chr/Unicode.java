package net.morimekta.console.chr;

/**
 * Unicode character representation. It represent the full 31-but unicode code
 * point, and will expand to the 2 surrogate paired string if necessary.
 */
public class Unicode implements Char {
    private final int cp;

    public Unicode(int cp) {
        this.cp = cp;
    }

    public Unicode(char ch) {
        this.cp = (int) ch;
    }

    @Override
    public int asInteger() {
        return cp;
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        builder.append('\'');
        switch (cp) {
            case NUL: return "<NUL>";
            case ABR: return "<ABR>";
            case EOF: return "<EOF>";
            case BEL: return "<BEL>";
            case BS:  return "<BS>";  // Not using '\b'. It conflicts with C definition of BEL.
            case VT:  return "<VT>";
            case ESC: return "<ESC>";
            case FS:  return "<FS>";
            case GS:  return "<GS>";
            case RS:  return "<RS>";
            case US:  return "<US>";
            case DEL: return "<DEL>";
            case TAB:
                builder.append('\\').append('t');
                break;
            case LF:
                builder.append('\\').append('n');
                break;
            case FF:
                builder.append('\\').append('f');
                break;
            case CR:
                builder.append('\\').append('r');
                break;
            case '"':
            case '\'':
            case '\\':
                builder.append('\\').append((char) cp);
                break;
            default:
                if (cp < 32) {
                    builder.append(String.format("\\%03o", (int) cp));
                } else if ((127 < cp && cp < 160) || (8192 <= cp && cp < 8448) || !Character.isDefined(cp)) {
                    if (Character.isBmpCodePoint(cp)) {
                        builder.append(String.format("\\u%04x", cp));
                    } else {
                        builder.append(String.format("\\u%04x", (int) Character.highSurrogate(cp)));
                        builder.append(String.format("\\u%04x", (int) Character.lowSurrogate(cp)));
                    }
                } else {
                    if (Character.isBmpCodePoint(cp)) {
                        builder.append((char) cp);
                    } else {
                        builder.append(Character.highSurrogate(cp));
                        builder.append(Character.lowSurrogate(cp));
                    }
                }
                break;
        }
        builder.append('\'');
        return builder.toString();
    }

    @Override
    public int printableWidth() {
        // Control characters.
        if (cp < 0x20 ||
            (0x7F <= cp && cp < 0xA0) ||
            cp == 0x061C ||
            (0x2000 <= cp && cp <= 0x2100) ||
            (0xFFF9 <= cp && cp <= 0xFFFB)) {
            return 0;
        }

        // TODO: There is also a set of non-width glyphs, e.g. used in some indian language.
        // These also have a printable width of 0, even though they are printed...

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
     * Make a uncode boder symbol matching the lines given. The four valies are for top
     * (upward line), right, bottom and left.
     *
     * 0 - no line.
     * 1 - thin line
     * 2 - thick line
     * 3 - double line
     *
     * @param u The upward line.
     * @param r The rightward line.
     * @param d The downward line.
     * @param l The leftward line.
     * @return The unicode instance, or null if none matching.
     */
    public static Unicode makeBorder(int u, int r, int d, int l) {
        if (u < 0 || u > 3 ||
            r < 0 || r > 3 ||
            d < 0 || d > 3 ||
            l < 0 || l > 3) {
            throw new IllegalArgumentException(String.format("No border possible for %d,%d,%d,%d.", u, r, d, l));
        }

        int id = (u << 24) |
                 (r << 16) |
                 (d << 8) |
                 (l);
        switch (id) {
            // Stumps
            case 0x0001:
                return new Unicode(0x2574);
            case 0x1000:
                return new Unicode(0x2575);
            case 0x0100:
                return new Unicode(0x2576);
            case 0x0010:
                return new Unicode(0x2577);
            case 0x0002:
                return new Unicode(0x2578);
            case 0x2000:
                return new Unicode(0x2579);
            case 0x0200:
                return new Unicode(0x257A);
            case 0x0020:
                return new Unicode(0x257B);

            // Lines (simple)
            case 0x0101:
                return new Unicode(0x2500);
            case 0x0202:
                return new Unicode(0x2501);
            case 0x1010:
                return new Unicode(0x2502);
            case 0x2020:
                return new Unicode(0x2503);

            // Half-and-half lines
            case 0x0201:
                return new Unicode(0x257C);
            case 0x1020:
                return new Unicode(0x257D);
            case 0x0102:
                return new Unicode(0x257E);
            case 0x2010:
                return new Unicode(0x257F);

            // Corners (simple)
            case 0x0110:
                return new Unicode(0x250C);
            case 0x0210:
                return new Unicode(0x250D);
            case 0x0120:
                return new Unicode(0x250E);
            case 0x0220:
                return new Unicode(0x250F);
            case 0x0011:
                return new Unicode(0x2510);
            case 0x0012:
                return new Unicode(0x2511);
            case 0x0021:
                return new Unicode(0x2512);
            case 0x0022:
                return new Unicode(0x2513);
            case 0x1100:
                return new Unicode(0x2514);
            case 0x1200:
                return new Unicode(0x2515);
            case 0x2100:
                return new Unicode(0x2516);
            case 0x2200:
                return new Unicode(0x2517);
            case 0x1001:
                return new Unicode(0x2518);
            case 0x1002:
                return new Unicode(0x2519);
            case 0x2001:
                return new Unicode(0x251A);
            case 0x2002:
                return new Unicode(0x251B);

            // T-crossings (urd)
            case 0x1110:
                return new Unicode(0x251C);
            case 0x1210:
                return new Unicode(0x251D);
            case 0x2110:
                return new Unicode(0x251E);
            case 0x1120:
                return new Unicode(0x251F);
            case 0x2120:
                return new Unicode(0x2520);
            case 0x2210:
                return new Unicode(0x2521);
            case 0x1220:
                return new Unicode(0x2522);
            case 0x2220:
                return new Unicode(0x2523);

            // T-crossings (udl)
            case 0x1011:
                return new Unicode(0x2514);
            case 0x1012:
                return new Unicode(0x2515);
            case 0x1021:
                return new Unicode(0x2516);
            case 0x2011:
                return new Unicode(0x2517);
            case 0x2021:
                return new Unicode(0x2528);
            case 0x2012:
                return new Unicode(0x2529);
            case 0x1022:
                return new Unicode(0x252A);
            case 0x2022:
                return new Unicode(0x252B);

            // T-crossings (rdl)
            case 0x0111:
                return new Unicode(0x251C);
            case 0x0112:
                return new Unicode(0x251D);
            case 0x0211:
                return new Unicode(0x251E);
            case 0x0212:
                return new Unicode(0x251F);
            case 0x0121:
                return new Unicode(0x2520);
            case 0x0122:
                return new Unicode(0x2521);
            case 0x0221:
                return new Unicode(0x2522);
            case 0x0222:
                return new Unicode(0x2523);

            // T-crossings (url)
            case 0x1101:
                return new Unicode(0x2524);
            case 0x1102:
                return new Unicode(0x2525);
            case 0x1201:
                return new Unicode(0x2526);
            case 0x1202:
                return new Unicode(0x2527);
            case 0x2101:
                return new Unicode(0x2528);
            case 0x2102:
                return new Unicode(0x2529);
            case 0x2201:
                return new Unicode(0x252A);
            case 0x2202:
                return new Unicode(0x252B);

            // Full Crosses
            case 0x1111:
                return new Unicode(0x253C);
            case 0x1112:
                return new Unicode(0x253D);
            case 0x1211:
                return new Unicode(0x253E);
            case 0x1212:
                return new Unicode(0x253F);
            case 0x2111:
                return new Unicode(0x2540);
            case 0x1121:
                return new Unicode(0x2541);
            case 0x2121:
                return new Unicode(0x2542);
            case 0x2112:
                return new Unicode(0x2543);
            case 0x2211:
                return new Unicode(0x2544);
            case 0x1122:
                return new Unicode(0x2545);
            case 0x1221:
                return new Unicode(0x2546);
            case 0x2212:
                return new Unicode(0x2547);
            case 0x1222:
                return new Unicode(0x2548);
            case 0x2122:
                return new Unicode(0x2549);
            case 0x2221:
                return new Unicode(0x254A);
            case 0x2222:
                return new Unicode(0x254B);

            // Double lines
            case 0x0303:
                return new Unicode(0x2550);
            case 0x3030:
                return new Unicode(0x2551);

            // Double and single+double corners.
            case 0x0310:
                return new Unicode(0x2552);
            case 0x0130:
                return new Unicode(0x2553);
            case 0x0330:
                return new Unicode(0x2554);
            case 0x0013:
                return new Unicode(0x2555);
            case 0x0031:
                return new Unicode(0x2556);
            case 0x0033:
                return new Unicode(0x2557);
            case 0x1300:
                return new Unicode(0x2558);
            case 0x3100:
                return new Unicode(0x2559);
            case 0x3300:
                return new Unicode(0x255A);
            case 0x1003:
                return new Unicode(0x255B);
            case 0x3001:
                return new Unicode(0x255C);
            case 0x3003:
                return new Unicode(0x255D);

            // Double and single+double T-crosses.
            case 0x1310:
                return new Unicode(0x255E);
            case 0x3130:
                return new Unicode(0x255F);
            case 0x3330:
                return new Unicode(0x2560);
            case 0x1013:
                return new Unicode(0x2561);
            case 0x3031:
                return new Unicode(0x2562);
            case 0x3033:
                return new Unicode(0x2563);
            case 0x0313:
                return new Unicode(0x2564);
            case 0x0131:
                return new Unicode(0x2565);
            case 0x0333:
                return new Unicode(0x2566);
            case 0x1303:
                return new Unicode(0x2567);
            case 0x3101:
                return new Unicode(0x2568);
            case 0x3303:
                return new Unicode(0x2569);

            // Double and single+double full-crosses.
            case 0x1313:
                return new Unicode(0x256A);
            case 0x3131:
                return new Unicode(0x256B);
            case 0x3333:
                return new Unicode(0x256C);
        }

        throw new IllegalArgumentException(String.format("No border for (u:%d,r:%d,d:%d,l:%d).", u, r, d, l));
    }

    /**
     * Gets the unicode char representing a circled number.
     * @param num Number to get unicode char for.
     * @return The unicode char representation.
     */
    public static Unicode makeNumeric(int num) {
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
        throw new IllegalArgumentException("No circled numeric for " + num);
    }

}
