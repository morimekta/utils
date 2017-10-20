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
package net.morimekta.util.json;

import net.morimekta.util.CharSlice;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Stein Eldar Johnsen
 * @since 19.10.15
 */
public class JsonToken extends CharSlice {
    public enum Type {
        // one of []{},:
        SYMBOL,
        // numerical
        NUMBER,
        // quoted literal
        LITERAL,
        // static token.
        TOKEN,
    }

    private static final char[] kNull  = new char[]{'n', 'u', 'l', 'l'};
    private static final char[] kTrue  = new char[]{'t', 'r', 'u', 'e'};
    private static final char[] kFalse = new char[]{'f', 'a', 'l', 's', 'e'};

    public static final char kListStart = '[';
    public static final char kListEnd   = ']';
    public static final char kListSep   = ',';
    public static final char kMapStart  = '{';
    public static final char kMapEnd    = '}';
    public static final char kKeyValSep = ':';

    static final char kDoubleQuote    = '\"';
    static final char kEscape         = '\\';
    static final char kSpace          = ' ';
    static final char kTab            = '\t';
    static final char kNewLine        = '\n';
    static final char kCarriageReturn = '\r';

    public final Type type;
    public final int  lineNo;
    public final int  linePos;

    public JsonToken(Type type, char[] lineBuffer, int offset, int len, int lineNo, int linePos) {
        super(lineBuffer, offset, len);
        this.type = type;
        this.lineNo = lineNo;
        this.linePos = linePos;
    }

    /**
     * @return The token type.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return The line number (1-indexed).
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * @return The start position on the line (0-indexed).
     */
    public int getLinePos() {
        return linePos;
    }

    public boolean isNull() {
        return type == Type.TOKEN && strEquals(kNull);
    }

    public boolean isSymbol() {
        return length() == 1 && "{}[],:".indexOf(charAt(0)) >= 0;
    }

    public final boolean isSymbol(char c) {
        return length() == 1 && charAt(0) == c;
    }

    public boolean isLiteral() {
        return type == Type.LITERAL && length() >= 2;
    }

    public boolean isBoolean() {
        return type == Type.TOKEN && strEquals(kTrue) || strEquals(kFalse);
    }

    public boolean isNumber() {
        return type == Type.NUMBER;
    }

    public boolean isInteger() {
        return type == Type.NUMBER && !containsAny('.', 'e', 'E');
    }

    public boolean isDouble() {
        return type == Type.NUMBER && containsAny('.', 'e', 'E');
    }

    public boolean booleanValue() {
        return strEquals(kTrue);
    }

    public byte byteValue() {
        return (byte) parseInteger();
    }

    public short shortValue() {
        return (short) parseInteger();
    }

    public int intValue() {
        return (int) parseInteger();
    }

    public long longValue() {
        return parseInteger();
    }

    public double doubleValue() {
        return parseDouble();
    }

    /**
     * Get the still quoted string literal content.
     *
     * @return The raw string literal.
     */
    public String rawJsonLiteral() {
        if (!isLiteral()) throw new IllegalStateException("No a string literal");
        return substring(1, -1).asString();
    }

    /**
     * Get the whole slice as a string.
     *
     * @return Slice decoded as UTF_8 string.
     */
    public String decodeJsonLiteral() {
        // This decodes the string from UTF_8 bytes.
        String raw = rawJsonLiteral();
        final int l = raw.length();
        StringBuilder out = new StringBuilder(l);

        boolean esc = false;
        for (int i = 0; i < l; ++i) {
            if (esc) {
                esc = false;

                char ch = raw.charAt(i);
                switch (ch) {
                    case 'b':
                        out.append('\b');
                        break;
                    case 'f':
                        out.append('\f');
                        break;
                    case 'n':
                        out.append('\n');
                        break;
                    case 'r':
                        out.append('\r');
                        break;
                    case 't':
                        out.append('\t');
                        break;
                    case '\"':
                    case '\\':
                        out.append(ch);
                        break;
                    case 'u':
                        if (l < i + 5) {
                            out.append('?');
                        } else {
                            String n = raw.substring(i + 1, i + 5);
                            try {
                                int cp = Integer.parseInt(n, 16);
                                out.append((char) cp);
                            } catch (NumberFormatException e) {
                                out.append('?');
                            }
                        }
                        i += 4;  // skipping 4 more characters.
                        break;
                    default:
                        out.append('?');
                        break;
                }
            } else if (raw.charAt(i) == '\\') {
                esc = true;
            } else {
                out.append(raw.charAt(i));
            }
        }
        return out.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(JsonToken.class, super.hashCode(), type, lineNo, linePos);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        JsonToken other = (JsonToken) o;

        return fb == other.fb &&
               off == other.off &&
               len == other.len &&
               type == other.type &&
               lineNo == other.lineNo &&
               linePos == other.linePos;
    }

    @Nonnull
    @Override
    public String toString() {
        return String.format("%s('%s',%d:%d-%d)", type.toString(), asString(), lineNo, linePos, linePos + length());
    }
}
