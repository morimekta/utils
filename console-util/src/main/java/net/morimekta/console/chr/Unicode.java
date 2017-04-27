/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.console.chr;

import static java.lang.Character.isBmpCodePoint;
import static net.morimekta.util.Strings.isConsolePrintable;

/**
 * Unicode character representation. It represent the full 31-but unicode code
 * point, and will expand to the 2 surrogate paired string if necessary.
 */
public class Unicode implements Char {
    private static final int NBSP_CP = 0x00A0;

    /**
     * No-break space.
     */
    public static final Unicode NBSP = new Unicode(NBSP_CP);

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
            case DEL: return "<DEL>";  // backspace??
            case NBSP_CP:
                return "<nbsp>";
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
            case '\"':
            case '\'':
            case '\\':
                builder.append('\\').append((char) cp);
                break;
            default:
                if (cp < 0x20) {
                    // Use
                    builder.append(String.format("\\%03o", (int) cp));
                } else if (isConsolePrintable(cp)) {
                    if (isBmpCodePoint(cp)) {
                        builder.append((char) cp);
                    } else {
                        builder.append(Character.highSurrogate(cp));
                        builder.append(Character.lowSurrogate(cp));
                    }
                } else {
                    if (isBmpCodePoint(cp)) {
                        builder.append(String.format("\\u%04x", cp));
                    } else {
                        builder.append(String.format("\\u%04x", (int) Character.highSurrogate(cp)));
                        builder.append(String.format("\\u%04x", (int) Character.lowSurrogate(cp)));
                    }
                }
                break;
        }
        return builder.toString();
    }

    @Override
    public int printableWidth() {
        if (cp < 0x0300) {
            // Control characters, accents, etc.
            if (cp < 0x20 ||
                (0x7F <= cp && cp <  0xA0)) {
                return 0;
            }
        } else if (cp < 0x0600) {
            if ((0x0300 <= cp && cp <  0x0370) || // 0-width punctuation
                (0x0483 <= cp && cp <= 0x0489) || // 0-width symbols
                (0x0591 <= cp && cp <= 0x05c7 &&  // 0-width sanskrit accents
                 cp != 0x05be && cp != 0x05c0 && cp != 0x05c3 && cp != 0x05c6)) {
                return 0;
            }
        } else if (cp < 0x0800) {
            if ((0x0600 <= cp && cp <= 0x0605) || // 0-width hebrew accents
                (0x0610 <= cp && cp <= 0x061a) || cp == 0x061c || // 0-width arabic accents
                cp == 0x064b ||
                (0x064c <= cp && cp <= 0x065f) || cp == 0x0670 ||  // 0-width arabic accents (2)
                (0x06d6 <= cp && cp <= 0x06ed && // 0-width symbols (2)
                 cp != 0x06de && cp != 0x06e5 && cp != 0x06e6 && cp != 0x06e9) ||
                cp == 0x070f || cp == 0x0711 ||
                (0x0730 <= cp && cp <= 0x074a) ||
                (0x07a6 <= cp && cp <= 0x07b0) ||
                (0x07eb <= cp && cp <= 0x07f3)) {
                return 0;
            }
        } else if (cp < 0x1000) {
            if ((0x0816 <= cp && cp <= 0x082d &&
                 cp != 0x081a && cp != 0x0824 && cp != 0x0828) ||
                cp == 0x0859 || cp == 0x085a || cp == 0x085b ||
                (0x08e3 <= cp && cp <= 0x0902) ||
                cp == 0x093a || cp == 0x093c ||
                (0x0941 <= cp && cp <= 0x0948) || cp == 0x094d ||
                (0x0951 <= cp && cp <= 0x0957) ||
                cp == 0x0962 || cp == 0x0963 ||
                cp == 0x0981 || cp == 0x09bc ||
                (0x09c1 <= cp && cp <= 0x09c4) ||
                cp == 0x09cd ||
                cp == 0x09e2 || cp == 0x09e3 ||
                cp == 0x0a01 || cp == 0x0a02 ||
                cp == 0x0a3c ||
                cp == 0x0a41 || cp == 0x0a42 ||
                cp == 0x0a47 || cp == 0x0a48 ||
                cp == 0x0a4b || cp == 0x0a4c || cp == 0x0a4d ||
                cp == 0x0a51 ||
                cp == 0x0a70 || cp == 0x0a71 ||
                cp == 0x0a75 ||
                cp == 0x0a81 || cp == 0x0a82 ||
                cp == 0x0abc ||
                (0x0ac1 <= cp && cp <= 0x0ac8 && cp != 0x0ac6) ||
                cp == 0x0acd ||
                cp == 0x0ae2 || cp == 0x0ae3 ||
                cp == 0x0b01 ||
                cp == 0x0b3c ||
                cp == 0x0b3f ||
                cp == 0x0b41 || cp == 0x0b42 || cp == 0x0b43 || cp == 0x0b44 ||
                cp == 0x0b4d ||
                cp == 0x0b56 ||
                cp == 0x0b62 || cp == 0x0b63 ||
                cp == 0x0b82 ||
                cp == 0x0bc0 ||
                cp == 0x0bcd ||
                cp == 0x0c00 ||
                cp == 0x0c3e || cp == 0x0c3f ||
                cp == 0x0c40 ||
                cp == 0x0c46 || cp == 0x0c47 || cp == 0x0c48 ||
                cp == 0x0c4a || cp == 0x0c4b || cp == 0x0c4c || cp == 0x0c4d ||
                cp == 0x0c55 || cp == 0x0c56 ||
                cp == 0x0c62 || cp == 0x0c63 ||
                cp == 0x0c81 ||
                cp == 0x0cbc || cp == 0x0cbf ||
                cp == 0x0cc6 ||
                cp == 0x0ccc || cp == 0x0ccd ||
                cp == 0x0ce2 || cp == 0x0ce3 ||
                cp == 0x0d01 ||
                cp == 0x0d41 || cp == 0x0d42 || cp == 0x0d43 || cp == 0x0d44 ||
                cp == 0x0d4d ||
                cp == 0x0d62 || cp == 0x0d63 ||
                cp == 0x0dca ||
                cp == 0x0dd2 || cp == 0x0dd3 ||
                cp == 0x0dd4 || cp == 0x0dd6 ||
                cp == 0x0e31 ||
                (0x0e34 <= cp && cp <= 0x0e3a) ||
                (0x0e47 <= cp && cp <= 0x0e4e) ||
                cp == 0x0eb1 ||
                (0x0eb4 <= cp && cp <= 0x0ebc && cp != 0x0eba) ||
                (0x0ec8 <= cp && cp <= 0x0ecd) ||
                cp == 0x0f18 || cp == 0x0f19 ||
                cp == 0x0f35 ||
                cp == 0x0f37 ||
                cp == 0x0f39 ||
                (0x0f71 <= cp && cp <= 0x0f87 && cp != 0x0f7f && cp != 0x0f85) ||
                (0x0f8d <= cp && cp <= 0x0fbc && cp != 0x0f98) ||
                cp == 0x0fc6) {
                return 0;
            }
        } else if (cp < 0x2000) {
            if (cp < 0x1360) {
                if (0x1100 <= cp && cp < 0x1160) {
                    return 2;
                } else if ((0x102d <= cp && cp <= 0x1037 && cp != 0x1031) ||
                    cp == 0x1039 ||
                    cp == 0x103a ||
                    cp == 0x103d || cp == 0x103e ||
                    cp == 0x1058 || cp == 0x1059 ||
                    cp == 0x105e || cp == 0x105f ||
                    cp == 0x1060 ||
                    cp == 0x1071 || cp == 0x1072 || cp == 0x1073 || cp == 0x1074 ||
                    cp == 0x1082 ||
                    cp == 0x1085 || cp == 0x1086 ||
                    cp == 0x108d ||
                    cp == 0x109d ||
                    (0x1160 <= cp && cp <= 0x11ff) ||
                    cp == 0x135d || cp == 0x135e || cp == 0x135f) {
                    return 0;
                }
            } else if (0x1700 < cp) {
                if ((0x1712 <= cp && cp <= 0x1714) ||
                    cp == 0x1732 || cp == 0x1733 || cp == 0x1734 ||
                    cp == 0x1752 || cp == 0x1753 ||
                    cp == 0x1772 || cp == 0x1773 ||
                    (0x17b4 <= cp && cp <= 0x17bd && cp != 0x17b6) ||
                    cp == 0x17c6 ||
                    (0x17c9 <= cp && cp <= 0x17d3) ||
                    cp == 0x17dd ||
                    (0x180b <= cp && cp <= 0x180e) ||
                    cp == 0x18a9 ||
                    (0x1920 <= cp && cp <= 0x1922) ||
                    cp == 0x1927 || cp == 0x1928 ||
                    cp == 0x1932 ||
                    cp == 0x1939 || cp == 0x193a || cp == 0x193b ||

                    cp == 0x1a17 || cp == 0x1a18 ||
                    cp == 0x1a1b ||
                    cp == 0x1a56 ||
                    (0x1a58 <= cp && cp <= 0x1a5e) ||
                    cp == 0x1a60 ||
                    cp == 0x1a62 ||
                    (0x1a65 <= cp && cp <= 0x1a6c) ||
                    (0x1a73 <= cp && cp <= 0x1a7c) ||
                    cp == 0x1a7f ||
                    (0x1ab0 <= cp && cp <= 0x1abe) ||
                    (0x1b00 <= cp && cp <= 0x1b03) ||
                    cp == 0x1b34 ||
                    (0x1b36 <= cp && cp <= 0x1b3a) ||
                    cp == 0x1b3c ||
                    cp == 0x1b42 ||
                    (0x1b6b <= cp && cp <= 0x1b73) ||
                    cp == 0x1b80 || cp == 0x1b81 ||
                    (0x1ba2 <= cp && cp <= 0x1ba5) ||
                    cp == 0x1ba8 || cp == 0x1ba9 ||
                    cp == 0x1bab || cp == 0x1bac || cp == 0x1bad ||
                    cp == 0x1be6 ||
                    cp == 0x1be8 || cp == 0x1be9 ||
                    cp == 0x1bed ||
                    cp == 0x1bef ||
                    cp == 0x1bf0 || cp == 0x1bf1 ||
                    (0x1c2c <= cp && cp <= 0x1c33) ||
                    cp == 0x1c36 || cp == 0x1c37 ||
                    (0x1cd0 <= cp && cp <= 0x1ce8 &&
                     cp != 0x1cd3 && cp != 0x1ce1) ||
                    cp == 0x1ced ||
                    cp == 0x1cf4 ||
                    cp == 0x1cf8 || cp == 0x1cf9 ||
                    (0x1dc0 <= cp && cp <= 0x1df5) ||
                    (0x1dfc <= cp && cp <= 0x1dff)) {
                    return 0;
                }
            }
        } else if (cp < 0x3000) {
            if ((0x200b <= cp && cp <= 0x200f) ||
                (0x202a <= cp && cp <= 0x202e) ||
                (0x2060 <= cp && cp <= 0x206f && cp != 0x2065) ||
                cp == 0x2068 ||
                (0x20d0 <= cp && cp <= 0x20ef) ||
                cp == 0x20f0 ||
                cp == 0x2d7f ||
                cp == 0x2cef || cp == 0x2cf0 || cp == 0x2cf1 ||
                (0x2de0 <= cp && cp <= 0x2dff)) {
                return 0;
            } else if ((0x00002329 <= cp && cp < 0x0000232b) ||
                       (0x00002e80 <= cp && cp < 0x00002fd6) ||
                       (0x00002ff0 <= cp && cp < 0x00002ffc)) {
                if (!(cp == 0x2e9a ||
                      (0x2ef4 <= cp && cp <= 0x2eff))) {
                    return 2;
                }
            }
        } else if (cp <= 0x10000) {
            // Control characters, accents, etc.
            if (cp < 0x3030) {
                if (cp == 0x302a ||
                    cp == 0x302b ||
                    cp == 0x3099 ||
                    cp == 0x309a ||
                    cp == 0x302c || cp == 0x302d ||
                    cp == 0x3099 || cp == 0x309a) {
                    return 0;
                }
            } else if (0xa660 < cp) {
                if ((0xa66f <= cp && cp <= 0xa67d &&
                     cp != 0xa673) ||
                    cp == 0xa6f0 || cp == 0xa6f1 ||
                    cp == 0xa69e || cp == 0xa69f ||
                    cp == 0xa802 ||
                    cp == 0xa806 ||
                    cp == 0xa80b ||
                    cp == 0xa825 || cp == 0xa826 ||
                    cp == 0xa8c4 ||
                    (0xa8e0 <= cp && cp <= 0xa8f1) ||
                    (0xa926 <= cp && cp <= 0xa92d) ||
                    (0xa947 <= cp && cp <= 0xa951) ||
                    (0xa980 <= cp && cp <= 0xa982) ||
                    cp == 0xa9b3 ||
                    (0xa9b6 <= cp && cp <= 0xa9b9) ||
                    cp == 0xa9bc ||
                    cp == 0xa9e5 ||
                    (0xaa29 <= cp && cp <= 0xaa2e) ||
                    cp == 0xaa31 || cp == 0xaa32 ||
                    cp == 0xaa35 || cp == 0xaa36 ||
                    cp == 0xaa43 ||
                    cp == 0xaa4c ||
                    cp == 0xaa7c ||
                    cp == 0xaab0 ||
                    cp == 0xaab2 || cp == 0xaab3 || cp == 0xaab4 ||
                    cp == 0xaab7 || cp == 0xaab8 ||
                    cp == 0xaaec || cp == 0xaaed || cp == 0xaabe || cp == 0xaabf ||
                    cp == 0xaac1 ||
                    cp == 0xaaf6 ||
                    cp == 0xabe5 ||
                    cp == 0xabe8 ||
                    cp == 0xabed ||
                    (0xfe00 <= cp && cp <= 0xfe0f) ||
                    (0xfe20 <= cp && cp <= 0xfe2f) ||
                    cp == 0xfeff ||
                    (0xfff9 <= cp && cp <= 0xfffb)) {
                    return 0;
                }
            }
            // CJK compatibility (square symbols), Extension A
            if ((0x3000 <= cp && cp < 0x4dc0) && !(
                    cp == 0x302a || cp == 0x302b || cp == 0x302c ||
                    cp == 0x3040 ||
                    (0x3097 <= cp && cp <= 0x309a) ||
                    (0x3100 <= cp && cp <= 0x3104) ||
                    // cp == 0x309a || 0-width
                    cp == 0x312e ||
                    cp == 0x312f ||
                    cp == 0x3130 ||
                    cp == 0x303f ||
                    cp == 0x318f ||
                    (0x31bb <= cp && cp <= 0x31bf) ||
                    (0x31e4 <= cp && cp <= 0x31ef) ||
                    cp == 0x321f ||
                    cp == 0x32ff ||
                    (0x3248 <= cp && cp <= 0x324f))) {
                return 2;
            } else if (
                    (0x4e00 <= cp && cp < 0xa4c7) ||  // CJK Main group of ideographs) ￬ ￬
                    (0xa960 <= cp && cp < 0xa97d) ||
                    (0xac00 <= cp && cp < 0xd7a4) ||
                    (0xd800 <= cp && cp < 0xe000) ||
                    (0xf900 <= cp && cp < 0xfaff) ||
                    (0xfe10 <= cp && cp < 0xfe1a) ||
                    (0xfe30 <= cp && cp < 0xfe6c) ||
                    (0xff01 <= cp && cp < 0xff61) ||
                    (0xffe0 <= cp && cp < 0xffe7)) {
                // Some excluded chars and ranges.
                if (!(
                        (0xa48d <= cp && cp <= 0xa48f) ||
                        cp == 0xfe53 ||
                        cp == 0xfe67)) {
                    // CJK or other double-width character.
                    return 2;
                }
            }
        } else {
            // Character.isIdeographic(cp) does not mean the same thing as 'double width'.
            if ((0x00020000 <= cp && cp < 0x0002A6C0) ||  // CJK Extension B
                (0x0002A700 <= cp && cp < 0x0002CEB0)) {  // CJK Extension C, D, E
                return 2;
            }
        }

        return 1;
    }

    @Override
    public int length() {
        if (!isBmpCodePoint(cp)) {
            return 2;
        }
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }

        return asInteger() == ((Unicode) o).asInteger();
    }

    @Override
    public String toString() {
        if (!isBmpCodePoint(cp)) {
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

    @Override
    public int compareTo(Char o) {
        return Integer.compare(asInteger(), o.asInteger());
    }
}
