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
import net.morimekta.util.io.LineBufferedReader;
import net.morimekta.util.io.Utf8StreamReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import static java.lang.String.format;

/**
 * Class for tokenizing a JSON document and return one token at a time. It is
 * optimized for high volume JSON parsing, and avoids things like buffer
 * copying and utf-8 parsing. The tokens are just references back to the
 * original input buffer, so are not safe to use as reference (except for
 * token type) after the next token has been read.
 */
public class JsonTokenizer extends LineBufferedReader {
    private static final int DEFAULT_BUFFER_SIZE = 1 << 11; // 2048 chars --> 4kb

    private JsonToken unreadToken;

    /**
     * Create a JSON tokenizer that reads from the input steam. It will only
     * read as far as requested, and no bytes further. It has no checking of
     * whether the document follows the JSON standard, but will only accept
     * JSON formatted tokens.
     *
     * Note that the content is assumed to be separated with newlines, which
     * means that if multiple JSON contents are read from the same stream, they
     * MUST have a separating newline. A single JSON object may still have
     * newlines in it's stream.
     *
     * @param in Input stream to parse from.
     */
    public JsonTokenizer(InputStream in) {
        this(new Utf8StreamReader(in));
    }

    /**
     * Create a JSON tokenizer that reads from the input steam. It will only
     * read as far as requested, and no bytes further. It has no checking of
     * whether the document follows the JSON standard, but will only accept
     * JSON formatted tokens.
     *
     * Note that the content is assumed to be separated with newlines, which
     * means that if multiple JSON contents are read from the same stream, they
     * MUST have a separating newline. A single JSON object may still have
     * newlines in it's stream.
     *
     * @param in Input stream to parse from.
     * @param bufferSize The size of the char buffer. Default is 2048 chars
     *                   (4096 bytes).
     */
    public JsonTokenizer(InputStream in, int bufferSize) {
        this(new Utf8StreamReader(in), bufferSize);
    }

    /**
     * Create a JSON tokenizer that reads from the char reader. It will only
     * read as far as requested, and no bytes further. It has no checking of
     * whether the document follows the JSON standard, but will only accept
     * JSON formatted tokens.
     *
     * Note that the content is assumed to be separated with newlines, which
     * means that if multiple JSON contents are read from the same stream, they
     * MUST have a separating newline. A single JSON object may still have
     * newlines in it's stream.
     *
     * @param in Reader of content to parse.
     */
    public JsonTokenizer(Reader in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }


    /**
     * Create a JSON tokenizer that reads from the char reader. It will only
     * read as far as requested, and no bytes further. It has no checking of
     * whether the document follows the JSON standard, but will only accept
     * JSON formatted tokens.
     *
     * Note that the content is assumed to be separated with newlines, which
     * means that if multiple JSON contents are read from the same stream, they
     * MUST have a separating newline. A single JSON object may still have
     * newlines in it's stream.
     *
     * @param in Reader of content to parse.
     * @param bufferSize The size of the char buffer. Default is 2048 chars
     *                   (4096 bytes).
     */
    public JsonTokenizer(Reader in, int bufferSize) {
        super(in, bufferSize);
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
    @Nonnull
    public JsonToken expect(String message) throws JsonException, IOException {
        if (!hasNext()) {
            throw newParseException("Expected %s: Got end of file", message);
        }
        JsonToken tmp = unreadToken;
        unreadToken = null;
        return tmp;
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
    @Nonnull
    public JsonToken expectString(String message) throws IOException, JsonException {
        if (!hasNext()) {
            throw newParseException("Expected %s (string literal): Got end of file", message);
        } else {
            if (unreadToken.isLiteral()) {
                JsonToken tmp = unreadToken;
                unreadToken = null;
                return tmp;
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
    @Nonnull
    public JsonToken expectNumber(String message) throws IOException, JsonException {
        if (!hasNext()) {
            throw newParseException("Expected %s (number): Got end of file", message);
        } else {
            if (unreadToken.isInteger() || unreadToken.isDouble()) {
                JsonToken tmp = unreadToken;
                unreadToken = null;
                return tmp;
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
            if (symbols.length == 1) {
                throw newParseException("Expected %s ('%c'): Got end of file",
                                        message,
                                        symbols[0]);
            }

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

            if (symbols.length == 1) {
                throw newMismatchException("Expected %s ('%c'): but found '%s'",
                                           message,
                                           symbols[0],
                                           unreadToken.asString());
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
     * Return the rest of the current line. This is handy for handling unwanted content
     * after the last
     * @return The rest of the last read line. Not including leading and ending whitespaces,
     *         since these are allowed.
     * @throws IOException If unable to read the rest of the line.
     */
    @Nonnull
    @Deprecated
    public String restOfLine() throws IOException {
        return getRestOfLine();
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
    @Nonnull
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
    @Nullable
    public JsonToken next() throws IOException, JsonException {
        if (unreadToken != null) {
            JsonToken tmp = unreadToken;
            unreadToken = null;
            return tmp;
        }

        while (lastChar >= 0) {
            if (lastChar == 0) {
                if (!readNextChar()) {
                    break;
                }
            }
            if (lastChar == JsonToken.kNewLine ||
                lastChar == JsonToken.kCarriageReturn ||
                lastChar == JsonToken.kSpace ||
                lastChar == JsonToken.kTab) {
                lastChar = 0;
                continue;
            }

            if (lastChar == JsonToken.kDoubleQuote) {
                return nextString();
            } else if (lastChar == '.' || lastChar == '-' || (lastChar >= '0' && lastChar <= '9')) {
                return nextNumber();
            } else if (lastChar == '_' ||
                       (lastChar >= 'a' && lastChar <= 'z') ||
                       (lastChar >= 'A' && lastChar <= 'Z')) {
                return nextToken();
            } else if (lastChar < 0x20 || lastChar >= 0x7F) {
                // UTF-8 characters are only allowed inside JSON string literals.
                throw newParseException("Illegal character in JSON structure: '\\u%04x'", lastChar);
            } else {
                return nextSymbol();
            }
        }

        return null;
    }

    /**
     * Returns the last line in the buffer. Or empty string if not usable.
     *
     * @return The line string, not including the line-break.
     */
    @Nonnull
    @Deprecated
    public String getLastLine() {
        return getLine();
    }

    // --- INTERNAL ---

    @Nonnull
    private JsonToken nextSymbol() {
        lastChar = 0;
        return new JsonToken(JsonToken.Type.SYMBOL, buffer, bufferOffset, 1, lineNo, linePos);
    }

    @Nonnull
    private JsonToken nextToken() throws IOException {
        maybeConsolidateBuffer();

        int startPos = linePos;
        int startOffset = bufferOffset;
        int startLine = lineNo;
        int len = 0;
        while (lastChar == '_' || lastChar == '.' ||
               (lastChar >= '0' && lastChar <= '9') ||
               (lastChar >= 'a' && lastChar <= 'z') ||
               (lastChar >= 'A' && lastChar <= 'Z')) {
            ++len;
            if (!readNextChar()) {
                break;
            }
        }

        return new JsonToken(JsonToken.Type.TOKEN, buffer, startOffset, len, startLine, startPos);
    }

    @Nonnull
    private JsonToken nextNumber() throws IOException, JsonException {
        maybeConsolidateBuffer();
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
        int startOffset = bufferOffset;
        int startLine = lineNo;
        // number (any type).
        int len = 0;

        if (lastChar == '-') {
            // only base 10 decimals can be negative.
            ++len;
            if (!readNextChar()) {
                throw newParseException("Negative indicator without number");
            }

            if (!(lastChar == '.' || (lastChar >= '0' && lastChar <= '9'))) {
                throw newParseException("No decimal after negative indicator");
            }
        }

        // decimal part.
        while (lastChar >= '0' && lastChar <= '9') {
            ++len;
            // numbers are terminated by first non-numeric character.
            if (!readNextChar()) {
                break;
            }
        }
        // fraction part.
        if (lastChar == '.') {
            ++len;
            // numbers are terminated by first non-numeric character.
            if (readNextChar()) {
                while (lastChar >= '0' && lastChar <= '9') {
                    ++len;
                    // numbers are terminated by first non-numeric character.
                    if (!readNextChar()) {
                        break;
                    }
                }
            }
        }
        // exponent part.
        if (lastChar == 'e' || lastChar == 'E') {
            ++len;
            // numbers are terminated by first non-numeric character.
            if (!readNextChar()) {
                String tmp = new String(buffer, startOffset, len);
                throw newParseException("Badly terminated JSON exponent: '%s'", tmp);
            }

            // The exponent can be explicitly prefixed with both '+'
            // and '-'.
            if (lastChar == '-' || lastChar == '+') {
                ++len;
                // numbers are terminated by first non-numeric character.
                if (!readNextChar()) {
                    String tmp = new String(buffer, startOffset, len);
                    throw newParseException("Badly terminated JSON exponent: '%s'", tmp);
                }
            }
            if (lastChar >= '0' && lastChar <= '9') {
                while (lastChar >= '0' && lastChar <= '9') {
                    ++len;
                    // numbers are terminated by first non-numeric character.
                    if (!readNextChar()) {
                        break;
                    }
                }
            } else {
                String tmp = new String(buffer, startOffset, len + 1);
                throw newParseException("Badly terminated JSON exponent: '%s'", tmp);
            }
        }

        // A number must be terminated correctly: End of stream, space or a
        // symbol that may be after a value: ',' '}' ']'.
        if (lastChar < 0 ||
            lastChar == JsonToken.kListSep ||
            lastChar == JsonToken.kMapEnd ||
            lastChar == JsonToken.kListEnd ||
            lastChar == JsonToken.kSpace ||
            lastChar == JsonToken.kTab ||
            lastChar == JsonToken.kNewLine ||
            lastChar == JsonToken.kCarriageReturn) {
            return new JsonToken(JsonToken.Type.NUMBER, buffer, startOffset, len, startLine, startPos);
        } else {
            String tmp = new String(buffer, startOffset, len + 1);
            throw newParseException("Wrongly terminated JSON number: '%s'", tmp);
        }
    }

    @Nonnull
    private JsonToken nextString() throws IOException, JsonException {
        maybeConsolidateBuffer();

        // string literals may be longer than 128 bytes. We may need to build it.
        StringBuilder consolidatedString = null;

        int startPos = linePos;
        int startOffset = bufferOffset;

        boolean esc = false;
        for (; ; ) {
            if (!preLoaded && bufferOffset >= (bufferLimit - 1)) {
                if (consolidatedString == null) {
                    consolidatedString = new StringBuilder();
                }
                consolidatedString.append(buffer, startOffset, bufferOffset - startOffset + 1);
                startOffset = 0;
            }

            if (!readNextChar()) {
                throw newParseException("Unexpected end of stream in string literal");
            }

            if (esc) {
                esc = false;
            } else if (lastChar == JsonToken.kEscape) {
                esc = true;
            } else if (lastChar == JsonToken.kDoubleQuote) {
                break;
            } else if (lastChar == JsonToken.kNewLine) {
                throw newParseException("Unexpected newline in string literal");
            }
        }

        lastChar = 0;
        if (consolidatedString != null) {
            consolidatedString.append(buffer, 0, bufferOffset + 1);
            String result = consolidatedString.toString();
            return new JsonToken(JsonToken.Type.LITERAL,
                                 result.toCharArray(),
                                 0,
                                 result.length(),
                                 lineNo,
                                 startPos);
        } else {
            return new JsonToken(JsonToken.Type.LITERAL,
                                 buffer,
                                 startOffset,
                                 bufferOffset - startOffset + 1,
                                 lineNo,
                                 startPos);
        }
    }

    @Nonnull
    private JsonException newMismatchException(String format, Object... params) throws IOException {
        if (params.length > 0) {
            format = format(format, params);
        }
        return new JsonException(format, this, unreadToken);
    }

    @Nonnull
    private JsonException newParseException(String format, Object... params) throws IOException, JsonException {
        if (params.length > 0) {
            format = format(format, params);
        }
        return new JsonException(format, getLine(), lineNo, linePos + (lastChar < 0 ? 1 : 0), 1);
    }
}
