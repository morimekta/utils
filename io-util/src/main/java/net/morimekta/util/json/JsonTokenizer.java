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

import net.morimekta.util.Strings;
import net.morimekta.util.io.IOUtils;
import net.morimekta.util.io.Utf8StreamReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Class for tokenizing a JSON document and return one token at a time. It is
 * optimized for high volume JSON parsing, and avoids things like buffer
 * copying and utf-8 parsing. The tokens are just references back to the
 * original input buffer, so are not safe to use as reference (except for
 * token type) after the next token has been read.
 */
public class JsonTokenizer {
    private static final int CONSOLIDATE_LINE_ON = 1 << 7;  // 128

    private final InputStream           reader;
    private final ArrayList<String>     lines;
    private final ByteBuffer            lineBuffer;
    private final ByteArrayOutputStream stringBuffer;

    private int line;
    private int linePos;
    private int lastByte;

    private StringBuilder lineBuilder;
    private JsonToken     unreadToken;

    /**
     * Create a JSON tokenizer that reads from the input steam. It will only
     * read as far as requested, and no bytes further. It has no checking of
     * whether the document follows the JSON standard, but will only accept
     * JSON formatted tokens.
     *
     * @param in Input stream to parse.
     */
    public JsonTokenizer(InputStream in) {
        this.reader = in;
        this.line = 1;
        this.linePos = 0;
        this.lastByte = 0;

        this.lineBuffer = ByteBuffer.allocate(1 << 16);  // 64k
        this.stringBuffer = new ByteArrayOutputStream(1 << 12);  // 4k
        this.lines = new ArrayList<>(1024);

        this.lineBuilder = new StringBuilder();
        this.unreadToken = null;
    }

    /**
     * Expect a new JSON token on the stream.
     *
     * @param message Message to add to exception if there are no more JSON
     *                tokens on the stream.
     * @return The next token.
     * @throws JsonException If no more tokens, or the token is illegally
     *         formatted.
     * @throws IOException If unable to read from stream.
     */
    public JsonToken expect(String message) throws JsonException, IOException {
        if (!hasNext()) {
            throw newParseException("Expected %s: Got end of file", message);
        }
        return next();
    }

    /**
     * Expect a string literal JSON token. A string literal is a double-quote
     * delimited UTF-8 encoded, JSON-escaped string.
     *
     * @param message Message to add to exception if there are no more JSON
     *                tokens on the stream.
     * @return The string literal token.
     * @throws JsonException If no more tokens, or the token is illegally
     *         formatted.
     * @throws IOException If unable to read from stream.
     */
    public JsonToken expectString(String message) throws IOException, JsonException {
        if (!hasNext()) {
            throw newParseException("Expected %s (string literal): Got end of file", message);
        } else {
            if (unreadToken.isLiteral()) {
                return next();
            }

            throw newMismatchException("Expected %s (string literal): but found '%s'",
                                       message, unreadToken.asString());
        }
    }

    /**
     * Expect a JSON number. A number is a 64 bit integer (long), or a 64-bit
     * real (double) number. The long can be decimal (base-10), octal (base-8)
     * with '0' prefix, or hexadecimal (base-16) encoded with '0x' prefix, and
     * the double can be a decimal point number (-0.0), or a scientific notation
     * number (-0.0e-1).
     *
     * @param message Message to add to exception if there are no more JSON
     *                tokens on the stream.
     * @return The number token.
     * @throws JsonException If no more tokens, or the token is illegally
     *         formatted.
     * @throws IOException If unable to read from stream.
     */
    public JsonToken expectNumber(String message) throws IOException, JsonException {
        if (!hasNext()) {
            throw newParseException("Expected %s (number): Got end of file", message);
        } else {
            if (unreadToken.isInteger() || unreadToken.isDouble()) {
                return next();
            }

            throw newMismatchException("Expected %s (number): but found '%s'",
                                       message, unreadToken.asString(),
                                       message);
        }
    }

    /**
     * Expect a string literal JSON token. A string literal is a double-quote
     * delimited UTF-8 encoded, JSON-escaped string.
     *
     * @param message Message to add to exception if there are no more JSON
     *                tokens on the stream.
     * @param symbols List of symbol characters to expect. If
     * @return The symbol that was encountered.
     * @throws JsonException If no more tokens, or the token is illegally
     *         formatted.
     * @throws IOException If unable to read from stream.
     */
    public char expectSymbol(String message, char... symbols) throws IOException, JsonException {
        if (symbols.length == 0) {
            throw new IllegalArgumentException("No symbols to match.");
        }
        if (!hasNext()) {
            throw newParseException("Expected %s (one of ['%s']): Got end of file",
                                    message,
                                    Strings.joinP("', '", symbols));
        } else {
            for (char symbol : symbols) {
                if (unreadToken.isSymbol(symbol)) {
                    unreadToken = null;
                    return symbol;
                }
            }

            throw newMismatchException("Expected %s (one of ['%s']): but found '%s'",
                                       message,
                                       Strings.joinP("', '", symbols),
                                       unreadToken.asString());
        }
    }

    /**
     * Whether there is another token on the stream. This will read up until
     * it finds a JSON token, or until the stream ends.
     *
     * @return True if (and only if) there is at least one more token on the
     *         stream.
     * @throws JsonException If the next token is illegally formatted.
     * @throws IOException If unable to read from stream.
     */
    public boolean hasNext() throws IOException, JsonException {
        if (unreadToken == null) {
            unreadToken = next();
        }
        return unreadToken != null;
    }

    /**
     * Return the next token or throw an exception. Though it does not consume
     * that token.
     *
     * @param message Message to add to exception if there are no more JSON
     *                tokens on the stream.
     * @return The next token.
     * @throws JsonException If the next token is illegally formatted.
     * @throws IOException If unable to read from stream.
     */
    public JsonToken peek(String message) throws IOException, JsonException {
        if (!hasNext()) {
            throw newParseException("Expected %s: Got end of file", message);
        }
        return unreadToken;
    }

    /**
     * Returns the next token on the stream, or null if there are no more JSON
     * tokens on the stream.
     * @return The next token, or null.
     * @throws JsonException If the next token is illegally formatted.
     * @throws IOException If unable to read from stream.
     */
    public JsonToken next() throws IOException, JsonException {
        if (unreadToken != null) {
            JsonToken tmp = unreadToken;
            unreadToken = null;
            return tmp;
        }

        while (lastByte >= 0) {
            if (lastByte == 0) {
                if (lineBuffer.position() == (lineBuffer.capacity() - CONSOLIDATE_LINE_ON)) {
                    flushLineBuffer();
                }

                lastByte = reader.read();
                if (lastByte < 0) {
                    break;
                }

                if (lastByte != JsonToken.kNewLine &&
                    lastByte != JsonToken.kCarriageReturn) {
                    lineBuffer.put((byte) lastByte);
                    ++linePos;
                }
            }

            if (lastByte == JsonToken.kNewLine ||
                lastByte == JsonToken.kCarriageReturn) {
                // New line
                flushLineBuffer();

                lines.add(lineBuilder.toString());
                linePos = 0;
                lineBuilder = new StringBuilder();
                ++line;

                // Handle CR-LF character pairs as a single newline.
                if (lastByte == JsonToken.kCarriageReturn) {
                    lastByte = reader.read();
                    if (lastByte == JsonToken.kNewLine) {
                        lastByte = 0;
                    } else if (lastByte != JsonToken.kCarriageReturn){
                        lineBuffer.put((byte) lastByte);
                        ++linePos;
                    }
                } else {
                    lastByte = 0;
                }
            } else if (lastByte == JsonToken.kSpace ||
                       lastByte == JsonToken.kTab) {
                lastByte = 0;
            } else if (lastByte == JsonToken.kDoubleQuote) {
                return nextString();
            } else if (lastByte == '-' || (lastByte >= '0' && lastByte <= '9')) {
                return nextNumber();
            } else if (lastByte == JsonToken.kListStart ||
                       lastByte == JsonToken.kListEnd ||
                       lastByte == JsonToken.kMapStart ||
                       lastByte == JsonToken.kMapEnd ||
                       lastByte == JsonToken.kKeyValSep ||
                       lastByte == JsonToken.kListSep ||
                       lastByte == '=') {
                return nextSymbol();
            } else if (lastByte < 0x20 || lastByte >= 0x7F) {
                // UTF-8 characters are only allowed inside JSON string literals.
                throw newParseException("Illegal character in JSON structure: '\\u%04x'", lastByte);
            } else {
                return nextToken();
            }
        }

        return null;
    }

    /**
     * Returns the line requested.
     * @param line The line number (1 .. N)
     * @return The line string, not including the line-break.
     * @throws IOException If unable to read from the string.
     * @throws IllegalArgumentException If the line number requested is less
     *         than 1.
     */
    public String getLine(int line) throws IOException {
        if (line < 1) {
            throw new IllegalArgumentException("Invalid line number requested: " + line);
        }
        if (lines.size() >= line) {
            return lines.get(line - 1);
        } else {
            flushLineBuffer();
            lineBuilder.append(IOUtils.readString(new Utf8StreamReader(reader), JsonToken.kNewLine));
            String ln = lineBuilder.toString();
            lines.add(ln);
            return ln;
        }
    }

    // --- INTERNAL ---

    private JsonToken nextSymbol() {
        lastByte = 0;
        return new JsonToken(JsonToken.Type.SYMBOL, lineBuffer.array(), lineBuffer.position() - 1, 1, line, linePos);
    }

    private JsonToken nextToken() throws IOException {
        int startPos = linePos;
        int startOffset = lineBuffer.position() - 1;
        int len = 0;
        while (lastByte == '_' || lastByte == '.' ||
               (lastByte >= '0' && lastByte <= '9') ||
               (lastByte >= 'a' && lastByte <= 'z') ||
               (lastByte >= 'A' && lastByte <= 'Z')) {
            ++len;
            lastByte = reader.read();
            if (lastByte < 0) {
                break;
            }
            lineBuffer.put((byte) lastByte);
            ++linePos;
        }

        return new JsonToken(JsonToken.Type.TOKEN, lineBuffer.array(), startOffset, len, line, startPos);
    }

    protected JsonToken nextNumber() throws IOException, JsonException {
        // NOTE: This code is pretty messy because it is a full state-engine
        // to ensure that the parsed number follows the JSON number syntax.
        // Alternatives are:
        //
        // dec = -?0
        // dec = -?.0
        // dec = -?0.0
        // sci = (dec)[eE][+-]?[0-9]+
        //
        // Octal and hexadecimal numbers are not supported.
        //
        // It is programmed as a state-engine to be very efficient, but
        // correctly detect valid JSON (and what is invalid if not).

        int startPos = linePos;
        int startOffset = lineBuffer.position() - 1;
        // number (any type).
        int len = 0;

        if (lastByte == '-') {
            // only base 10 decimals can be negative.
            ++len;
            lastByte = reader.read();
            if (lastByte < 0) {
                throw newParseException("Negative indicator without number");
            }
            lineBuffer.put((byte) lastByte);
            ++linePos;

            if (!(lastByte == '.' || (lastByte >= '0' && lastByte <= '9'))) {
                throw newParseException("No decimal after negative indicator");
            }
        }

        // decimal part.
        while (lastByte >= '0' && lastByte <= '9') {
            ++len;
            // numbers are terminated by first non-numeric character.
            lastByte = reader.read();
            if (lastByte < 0) {
                break;
            }
            lineBuffer.put((byte) lastByte);
            ++linePos;
        }
        // fraction part.
        if (lastByte == '.') {
            ++len;
            // numbers are terminated by first non-numeric character.
            lastByte = reader.read();
            if (lastByte >= 0) {
                lineBuffer.put((byte) lastByte);
                ++linePos;

                while (lastByte >= '0' && lastByte <= '9') {
                    ++len;
                    // numbers are terminated by first non-numeric character.
                    lastByte = reader.read();
                    if (lastByte < 0) {
                        break;
                    }
                    lineBuffer.put((byte) lastByte);
                    ++linePos;
                }
            }
        }
        // exponent part.
        if (lastByte == 'e' || lastByte == 'E') {
            ++len;
            // numbers are terminated by first non-numeric character.
            lastByte = reader.read();
            if (lastByte >= 0) {
                lineBuffer.put((byte) lastByte);
                ++linePos;

                // The exponent can be explicitly prefixed with both '+'
                // and '-'.
                if (lastByte == '-' || lastByte == '+') {
                    ++len;
                    // numbers are terminated by first non-numeric character.
                    lastByte = reader.read();
                    if (lastByte >= 0) {
                        lineBuffer.put((byte) lastByte);
                        ++linePos;
                    }
                }

                while (lastByte >= '0' && lastByte <= '9') {
                    ++len;
                    // numbers are terminated by first non-numeric character.
                    lastByte = reader.read();
                    if (lastByte < 0) {
                        break;
                    }
                    lineBuffer.put((byte) lastByte);
                    ++linePos;
                }
            }
        }

        // A number must be terminated correctly: End of stream, space or a
        // symbol that may be after a value: ',' '}' ']'.
        if (lastByte < 0 ||
            lastByte == JsonToken.kListSep ||
            lastByte == JsonToken.kMapEnd ||
            lastByte == JsonToken.kListEnd ||
            lastByte == JsonToken.kSpace ||
            lastByte == JsonToken.kTab ||
            lastByte == JsonToken.kNewLine ||
            lastByte == JsonToken.kCarriageReturn) {
            return new JsonToken(JsonToken.Type.NUMBER, lineBuffer.array(), startOffset, len, line, startPos);
        } else {
            String tmp = new String(lineBuffer.array(), startOffset, len + 1, StandardCharsets.UTF_8);
            throw newParseException("Wrongly terminated JSON number: '%s'", tmp);
        }
    }

    private JsonToken nextString() throws IOException, JsonException {
        // string literals may be longer than 128 bytes. We may need to build it.
        stringBuffer.reset();
        stringBuffer.write(lastByte);

        int startPos = linePos;
        int startOffset = lineBuffer.position();

        boolean consolidated = false;
        boolean esc = false;
        for (; ; ) {
            if (lineBuffer.position() >= (lineBuffer.capacity() - 1)) {
                stringBuffer.write(lineBuffer.array(), startOffset, lineBuffer.position() - startOffset);
                startOffset = 0;
                consolidated = true;
                flushLineBuffer();
            }

            lastByte = reader.read();
            if (lastByte < 0) {
                throw newParseException("Unexpected end of stream in string literal");
            }

            lineBuffer.put((byte) lastByte);
            ++linePos;

            if (esc) {
                esc = false;
            } else if (lastByte == JsonToken.kEscape) {
                esc = true;
            } else if (lastByte == JsonToken.kDoubleQuote) {
                break;
            }
        }

        lastByte = 0;
        if (consolidated) {
            stringBuffer.write(lineBuffer.array(), 0, lineBuffer.position());
            return new JsonToken(JsonToken.Type.LITERAL,
                                 stringBuffer.toByteArray(),
                                 0,
                                 stringBuffer.size(),
                                 line,
                                 startPos);
        } else {
            return new JsonToken(JsonToken.Type.LITERAL,
                                 lineBuffer.array(),
                                 startOffset - 1,
                                 lineBuffer.position() - startOffset + 1,
                                 line,
                                 startPos);
        }
    }

    private void flushLineBuffer() {
        lineBuilder.append(new String(lineBuffer.array(), 0, lineBuffer.position(), StandardCharsets.UTF_8));
        lineBuffer.clear();
    }

    private JsonException newMismatchException(String format, Object... params) throws IOException {
        if (params.length > 0) {
            format = String.format(format, params);
        }
        return new JsonException(format, this, unreadToken);
    }

    private JsonException newParseException(String format, Object... params) throws IOException, JsonException {
        if (params.length > 0) {
            format = String.format(format, params);
        }
        return new JsonException(format, getLine(line), line, linePos, 1);
    }
}
