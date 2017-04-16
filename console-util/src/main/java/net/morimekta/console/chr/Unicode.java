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

/**
 * Unicode character representation. It represent the full 31-but unicode code
 * point, and will expand to the 2 surrogate paired string if necessary.
 */
public class Unicode implements Char {
    private static final int NBSP_CP = 0x00A0;
    private static final int ALM_CP = 0x061C;

    /**
     * No-break space.
     */
    public static final Unicode NBSP = new Unicode(NBSP_CP);
    public static final Unicode ALM = new Unicode(ALM_CP);

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
        if (cp == NBSP_CP) {
            return 1;
        }

        // Control characters.
        if (cp < 0x20 ||
            (0x7F <= cp && cp < 0xA0) ||
            cp == ALM_CP ||
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

    @Override
    public int compareTo(Char o) {
        return Integer.compare(asInteger(), o.asInteger());
    }
}
