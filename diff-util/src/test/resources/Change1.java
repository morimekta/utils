/*
 * Diff
 *
 * Copyright 2006 Google Inc.
 * http://code.google.com/p/google-diff-match-patch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.morimekta.diff;

import net.morimekta.util.Stringable;
import net.morimekta.util.Strings;

import java.util.Objects;

/**
 * Class representing one diff operation.
 */
public class Change implements Stringable {
    /**
     * One of: INSERT, DELETE or EQUAL.
     */
    public Operation operation;
    /**
     * The text associated with this diff operation.
     */
    public String    text;

    /**
     * Constructor.  Initializes the diff with the provided values.
     * @param operation One of INSERT, DELETE or EQUAL.
     * @param text The text being applied.
     */
    public Change(Operation operation, String text) {
        // Construct a diff with the specified operation and text.
        this.operation = operation;
        this.text = text;
    }

    /**
     * Display a human-readable version of this DiffBase.
     * @return text version.
     */
    public String toString() {
        return "Change(" + this.operation + ",\"" + Strings.escape(this.text) + "\")";
    }

    /**
     * Create a numeric hash value for a DiffBase.
     * This function is not used by DMP.
     * @return Hash value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Change.class, operation, text);
    }

    /**
     * Is this DiffBase equivalent to another DiffBase?
     * @param o Another Change to compare against.
     * @return true or false.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(getClass().equals(o.getClass()))) {
            return false;
        }
        Change other = (Change) o;
        return operation == other.operation &&
               Objects.equals(text, other.text);
    }

    @Override
    public String asString() {
        switch (operation) {
            case DELETE: return "-" + text;
            case INSERT: return "+" + text;
            default: return " " + text;
        }
    }
}
