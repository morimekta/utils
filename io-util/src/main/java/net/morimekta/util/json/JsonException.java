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

import net.morimekta.util.Stringable;
import net.morimekta.util.Strings;

import java.io.IOException;

/**
 * Exception in parsing JSON.
 */
public class JsonException extends Exception implements Stringable {
    private final String line;
    private final int    lineNo;
    private final int    linePos;
    private final int    len;

    public JsonException(String message, String line, int lineNo, int linePos, int len) {
        super(message);

        this.line = line;
        this.lineNo = lineNo;
        this.linePos = linePos;
        this.len = len;
    }

    public JsonException(String message, JsonTokenizer tokenizer, JsonToken token) throws IOException {
        super(message);

        line = tokenizer.getLine(token.lineNo);
        lineNo = token.lineNo;
        linePos = token.linePos;
        len = token.asString().length();
    }

    /**
     * @return The full utf-8 line (no ending line break) of the exception.
     */
    public String getLine() {
        return line;
    }

    /**
     * @return The 1-indexed line number of the exception.
     *         If not representing a token, will be 0.
     */
    public int getLineNo() {
        return lineNo;
    }

    /**
     * Get the line position, 1-indexed of the first character that is part
     * of the exception. If not representing a token, will be 0.
     *
     * @return Line position.
     */
    public int getLinePos() {
        return linePos;
    }

    /**
     * @return The number of chars representing the token causing the exception.
     *         Will be at least 1 for anything representing a token.
     *         If not representing a token, will be 0.
     */
    public int getLen() {
        return len;
    }

    @Override
    public String asString() {
        if (len > 0) {
            return String.format("JSON Error on line %d: %s%n" +
                                 "%s%n" +
                                 "%s%s",
                                 getLineNo(),
                                 getLocalizedMessage(),
                                 getLine(),
                                 Strings.times("-", getLinePos() - 1),
                                 Strings.times("^", getLen()));
        } else if (lineNo > 0) {
            return String.format("JSON Error on line %d: %s%n" +
                                 "%s%n" +
                                 "%s^",
                                 getLineNo(),
                                 getLocalizedMessage(),
                                 getLine(),
                                 Strings.times("-", getLinePos()));
        } else {
            return String.format("JSON Error: %s", getLocalizedMessage());
        }
    }

    @Override
    public String toString() {
        return "JSON Error: " + getMessage();
    }
}
