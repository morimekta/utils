package net.morimekta.util;

import java.util.stream.IntStream;

/**
 * Extra stream utilities.
 */
public class ExtraStreams {
    /**
     * Make a range stream from 0 to (but not including) N.
     *
     * @param N the number of iterations.
     * @return The stream.
     */
    public static IntStream times(int N) {
        if (N <= 0) {
            throw new IllegalArgumentException("Invalid recurrence count: " + N);
        }
        return IntStream.range(0, N);
    }

    // PRIVATE constructor to defeat instantiation.
    private ExtraStreams() {}
}
