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
        return new Diff(changesFromDelta(text1, delta), DiffOptions.defaults());
    }

    private final LinkedList<Change> changeList;
}
