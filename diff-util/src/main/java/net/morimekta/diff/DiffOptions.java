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

import java.time.Clock;

/**
 * Options for diff.
 */
public class DiffOptions {
    private double  timeout    = 1.0;
    private int     editCost   = 4;
    private boolean checkLines = true;
    private Clock   clock      = Clock.systemUTC();

    /**
     * Get the default diff options.
     *
     * @return The doff options.
     */
    public static DiffOptions defaults() {
        return new DiffOptions();
    }

    /**
     * Set the number of seconds to map a diff before giving up (0 for infinity).
     *
     * @param timeout The number of seconds.
     * @return The options.
     */
    public DiffOptions withTimeout(double timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Set the cost of an empty edit operation in terms of edit characters
     *
     * @param editCost The cost value.
     * @return The options.
     */
    public DiffOptions withEditCost(int editCost) {
        this.editCost = editCost;
        return this;
    }

    public DiffOptions withChangeLines(boolean changeLines) {
        this.checkLines = changeLines;
        return this;
    }

    public DiffOptions withClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Number of seconds to map a diff before giving up (0 for infinity).
     */
    public double getTimeout() {
        return timeout;
    }

    /**
     * Cost of an empty edit operation in terms of edit characters.
     */
    public int getEditCost() {
        return editCost;
    }

    public boolean getCheckLines() {
        return checkLines;
    }

    public Clock getClock() {
        return clock;
    }
}
