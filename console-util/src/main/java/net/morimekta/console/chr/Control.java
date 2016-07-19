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

import net.morimekta.util.Strings;

/**
 *
 * https://en.wikipedia.org/wiki/C0_and_C1_control_codes
 */
public class Control implements Char {
    public static final Control UP    = new Control("\033[A");
    public static final Control DOWN  = new Control("\033[B");
    public static final Control RIGHT = new Control("\033[C");
    public static final Control LEFT  = new Control("\033[D");

    public static final Control CTRL_UP    = new Control("\033[1;5A");
    public static final Control CTRL_DOWN  = new Control("\033[1;5B");
    public static final Control CTRL_RIGHT = new Control("\033[1;5C");
    public static final Control CTRL_LEFT  = new Control("\033[1;5D");

    public static final Control CURSOR_ERASE   = new Control("\033[K");
    public static final Control CURSOR_SAVE    = new Control("\033[s");
    public static final Control CURSOR_RESTORE = new Control("\033[u");

    public static final Control DPAD_MID = new Control("\033[E");

    public static final Control INSERT    = new Control("\033[2~");
    public static final Control DELETE    = new Control("\033[3~");
    public static final Control HOME      = new Control("\033[1~");
    public static final Control END       = new Control("\033[4~");
    public static final Control PAGE_UP   = new Control("\033[5~");
    public static final Control PAGE_DOWN = new Control("\033[6~");

    public static final Control F1 = new Control("\033OP");
    public static final Control F2 = new Control("\033OQ");
    public static final Control F3 = new Control("\033OR");
    public static final Control F4 = new Control("\033OS");
    public static final Control F5 = new Control("\033[15~");
    public static final Control F6 = new Control("\033[17~");
    public static final Control F7 = new Control("\033[18~");
    public static final Control F8 = new Control("\033[19~");
    public static final Control F9 = new Control("\033[20~");

    private final String str;

    public Control(CharSequence str) {
        if (str.equals("\033OH")) {
            this.str = "\033[1~";  // HOME
        } else if (str.equals("\033OF")) {
            this.str = "\033[4~";  // END
        } else {
            this.str = str.toString();
        }
    }

    public static Control cursorSetPos(int line) {
        return cursorSetPos(line, 0);
    }

    public static Control cursorSetPos(int line, int col) {
        return new Control(String.format("\033[%d;%dH", line, col));
    }

    public static Control cursorUp(int num) {
        return new Control(String.format("\033[%dA", num));
    }

    public static Control cursorDown(int num) {
        return new Control(String.format("\033[%dB", num));
    }

    public static Control cursorRight(int num) {
        return new Control(String.format("\033[%dC", num));
    }

    public static Control cursorLeft(int num) {
        return new Control(String.format("\033[%dD", num));
    }

    @Override
    public int asInteger() {
        return -1;
    }

    @Override
    public String asString() {
        /*--*/ if (str.equals(UP.str)) {
            return "<up>";
        } else if (str.equals(DOWN.str)) {
            return "<down>";
        } else if (str.equals(RIGHT.str)) {
            return "<right>";
        } else if (str.equals(LEFT.str)) {
            return "<left>";
        } else if (str.equals(CTRL_UP.str)) {
            return "<C-up>";
        } else if (str.equals(CTRL_DOWN.str)) {
            return "<C-down>";
        } else if (str.equals(CTRL_RIGHT.str)) {
            return "<C-right>";
        } else if (str.equals(CTRL_LEFT.str)) {
            return "<C-left>";
        } else if (str.equals(CURSOR_ERASE.str)) {
            return "<cursor-erase>";
        } else if (str.equals(CURSOR_SAVE.str)) {
            return "<cursor-save>";
        } else if (str.equals(CURSOR_RESTORE.str)) {
            return "<cursor-restore>";
        } else if (str.equals(DPAD_MID.str)) {
            return "<dpa-mid>";
        } else if (str.equals(INSERT.str)) {
            return "<insert>";
        } else if (str.equals(DELETE.str)) {
            return "<delete>";
        } else if (str.equals(HOME.str)) {
            return "<home>";
        } else if (str.equals(END.str)) {
            return "<end>";
        } else if (str.equals(PAGE_UP.str)) {
            return "<pg-up>";
        } else if (str.equals(PAGE_DOWN.str)) {
            return "<pg-down>";
        } else if (str.equals(F1.str)) {
            return "<F1>";
        } else if (str.equals(F2.str)) {
            return "<F2>";
        } else if (str.equals(F3.str)) {
            return "<F3>";
        } else if (str.equals(F4.str)) {
            return "<F4>";
        } else if (str.equals(F5.str)) {
            return "<F5>";
        } else if (str.equals(F6.str)) {
            return "<F6>";
        } else if (str.equals(F7.str)) {
            return "<F7>";
        } else if (str.equals(F8.str)) {
            return "<F8>";
        } else if (str.equals(F9.str)) {
            return "<F9>";
        } else if (str.length() == 2 &&
                   str.charAt(1) >= 'a' &&
                   str.charAt(1) <= 'z') {
            return "<M-" + str.charAt(1) + '>';
        }
        return Strings.escape(str);
    }

    @Override
    public int printableWidth() {
        return 0;
    }

    @Override
    public int length() {
        return str.length();
    }

    @Override
    public String toString() {
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        Control other = (Control) o;

        return str.equals(other.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public int compareTo(Char o) {
        if (o instanceof Control) {
            return str.compareTo(((Control) o).str);
        }
        return Integer.compare(asInteger(), o.asInteger());
    }
}
