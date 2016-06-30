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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedList;

/**
 * Diff.
 */
public class Diff extends DiffBase {

    public Diff(String text1, String text2) {
        this(text1, text2, DiffOptions.defaults());
    }

    public Diff(String text1, String text2, DiffOptions options) {
        this(text1, text2, options, getDeadline(options));
    }

    public Diff(String text1, String text2, DiffOptions options, long deadline) {
        super(options, deadline);

        // Check for null inputs.
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("Null inputs. (diff_main)");
        }

        this.changeList = main(text1, text2, options.getCheckLines());
    }

    Diff(LinkedList<Change> changeList, DiffOptions options) {
        super(options, getDeadline(options));
        this.changeList = changeList;
    }

    @Override
    public LinkedList<Change> getChangeList() {
        return changeList;
    }

    /**
     * Given the original text1, and an encoded string which describes the
     * operations required to transform text1 into text2, compute the full diff.
     *
     * @param text1 Source string for the diff.
     * @param delta Delta text.
     * @return Diff object.
     * @throws IllegalArgumentException If invalid input.
     */
    public static Diff fromDelta(String text1, String delta)
            throws IllegalArgumentException {
        LinkedList<Change> diffs = new LinkedList<>();
        int pointer = 0;  // Cursor in text1
        String[] tokens = delta.split("\t");
        for (String token : tokens) {
            if (token.length() == 0) {
                // Blank tokens are ok (from a trailing \t).
                continue;
            }
            // Each token begins with a one character parameter which specifies the
            // operation of this token (delete, insert, equality).
            String param = token.substring(1);
            switch (token.charAt(0)) {
                case '+':
                    // decode would change all "+" to " "
                    param = param.replace("+", "%2B");
                    try {
                        param = URLDecoder.decode(param, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // Not likely on modern system.
                        throw new Error("This system does not support UTF-8.", e);
                    } catch (IllegalArgumentException e) {
                        // Malformed URI sequence.
                        throw new IllegalArgumentException(
                                "Illegal escape in diff_fromDelta: " + param, e);
                    }
                    diffs.add(new Change(Operation.INSERT, param));
                    break;
                case '-':
                    // Fall through.
                case '=':
                    int n;
                    try {
                        n = Integer.parseInt(param);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "Invalid number in diff_fromDelta: " + param, e);
                    }
                    if (n < 0) {
                        throw new IllegalArgumentException(
                                "Negative number in diff_fromDelta: " + param);
                    }
                    String text;
                    try {
                        text = text1.substring(pointer, pointer += n);
                    } catch (StringIndexOutOfBoundsException e) {
                        throw new IllegalArgumentException("Delta length (" + pointer
                                                           + ") larger than source text length (" + text1.length()
                                                           + ").", e);
                    }
                    if (token.charAt(0) == '=') {
                        diffs.add(new Change(Operation.EQUAL, text));
                    } else {
                        diffs.add(new Change(Operation.DELETE, text));
                    }
                    break;
                default:
                    // Anything else is an error.
                    throw new IllegalArgumentException(
                            "Invalid diff operation in diff_fromDelta: " + token.charAt(0));
            }
        }
        if (pointer != text1.length()) {
            throw new IllegalArgumentException("Delta length (" + pointer
                                               + ") smaller than source text length (" + text1.length() + ").");
        }

        return new Diff(diffs, DiffOptions.defaults());
    }

    private final LinkedList<Change> changeList;
}
