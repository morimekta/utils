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

import net.morimekta.util.Numeric;
import net.morimekta.util.Stringable;

/**
 * General interface implemented by character-like classes. A char
 * can represent an actual character (Unicode), or a control sequence
 * (Control, Color) that can alter the look or behaviour of the console
 * the application runs in.
 */
public interface Char extends Stringable, Numeric, Comparable<Char> {
    /** Null. */
    char NUL = '\0';
    /** Abort - [Control c] */
    char ABR = '\003';
    /** Abort - [Control d] */
    char EOF = '\004';
    /** Bell. */
    char BEL = '\007';
    /** Backspace: Not used on Linux */
    char BS  = '\010';
    /** Horizontal Tab. */
    char TAB = '\t';
    /** Line Feed (newline). */
    char LF  = '\n';
    /** Vertical Tab. */
    char VT  = '\013';
    /** Form Feed. */
    char FF  = '\f';
    /** Carriage Return. */
    char CR  = '\r';
    /** Escape. */
    char ESC = '\033';

    /** File Separator. */
    char FS  = '\034';
    /** Group Separator. */
    char GS  = '\035';
    /** Record Separator. */
    char RS  = '\036';
    /** Unit Separator. */
    char US  = '\037';
    /**
     * DEL, A.K.A. "Backspace"
     *
     * Note: This is a Linux keyboard weirdness, that backspace key returns
     * DEL (\0177), not BS (\010), which is the ASCII control char made
     * specifically to represent that key...
     */
    char DEL = '\177';

    /**
     * Unicode codepoint representing this character.
     *
     * @return The 31 bit unsigned unicode code-point, or -1 if not
     *         representing a single unicode character.
     */
    @Override
    int asInteger();

    /**
     * The number of character spaces taken up by this symbol. Usually 1, but
     * 0 for control sequences, control characters, and 2 for wide symbols like
     * CJK characters.
     *
     * @return The printable width of the character.
     */
    int printableWidth();

    /**
     * Number of utf-16 characters that this character takes up if enclosed
     * in a java string.
     *
     * @return The char count of the character.
     */
    int length();
}
