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

import java.util.LinkedList;

/**
 * Find the 'middle snake' of a diff, split the problem in two
 * and return the recursively constructed diff.
 * See Myers 1986 paper: An O(ND) Difference Algorithm and Its Variations.
 */
public class Bisect extends DiffBase {
    /**
     * Bisect two strings.
     *
     * @param text1 Source (old) string.
     * @param text2 Target (new) string.
     */
    public Bisect(String text1, String text2) {
        this(text1, text2, DiffOptions.defaults());
    }

    /**
     * Bisect two strings.
     * @param text1 Source (old) string.
     * @param text2 Target (new) string.
     * @param options DiffOptions to use.
     */
    public Bisect(String text1, String text2, DiffOptions options) {
        this(text1, text2, options, getDeadline(options));
    }

    Bisect(String text1, String text2, DiffOptions options, long deadline) {
        super(options, deadline);

        // Check for null inputs.
        if (text1 == null || text2 == null) {
            throw new IllegalArgumentException("Null inputs. (diff_main)");
        }

        this.changeList = bisect(text1, text2);
    }

    Bisect(LinkedList<Change> changeList) {
        super(DiffOptions.defaults(), 0);
        this.changeList = changeList;
    }


    @Override
    public LinkedList<Change> getChangeList() {
        return changeList;
    }

    /**
     * Given the original text1, and an encoded string which describes the
     * operations required to transform text1 into text2, compute the full diff.
     * @param text1 Source string for the diff.
     * @param delta Delta text.
     * @return Array of DiffBase objects or null if invalid.
     * @throws IllegalArgumentException If invalid input.
     */
    public static Bisect fromDelta(String text1, String delta)
            throws IllegalArgumentException {
        return new Bisect(changesFromDelta(text1, delta));
    }

    private final LinkedList<Change> changeList;
}
