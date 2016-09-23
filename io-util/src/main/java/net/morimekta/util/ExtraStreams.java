package net.morimekta.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Extra stream utilities.
 */
public class ExtraStreams {
    /**
     * Make an integer range stream using the standard
     * [start and include -&gt; end not including) definition with
     * an increment of 1.
     *
     * @param start The starting int.
     * @param limit The max limit.
     * @return The stream.
     */
    public static IntStream range(int start, int limit) {
        return range(start, limit, 1, false);
    }

    /**
     * Make an integer range stream using the standard
     * [start and include -&gt; end not including) definition.
     *
     * @param start The starting int.
     * @param limit The max limit.
     * @param increment The increment for each step.
     * @return The stream.
     */
    public static IntStream range(int start, int limit, int increment) {
        return range(start, limit, increment, false);
    }

    /**
     * Make an integer range stream using the standard
     * [start and include -&gt; end not including) definition.
     *
     * @param start The starting int.
     * @param limit The max limit.
     * @param increment The increment for each step.
     * @param parallel If the range can be split into parallel fork-join threads.
     * @return The stream.
     */
    public static IntStream range(int start, int limit, int increment, boolean parallel) {
        return StreamSupport.intStream(new IntRange(start, limit, increment), parallel);
    }

    /**
     * Make a range stream from 0 to (but not including) N.
     *
     * @param N the number of iterations.
     * @return The stream.
     */
    public static IntStream times(int N) {
        return StreamSupport.intStream(new IntRange(0, N, 1), false);
    }

    /**
     * Range spliterator implementation.
     */
    public static class IntRange implements Spliterator.OfInt {
        private final AtomicInteger current;
        private final int limit;
        private final int increment;

        /**
         * Create a range from start with limit and increment step.
         * @param start The starting value.
         * @param limit The upper limit (non-inclusive)
         * @param increment The increment step.
         */
        public IntRange(int start, int limit, int increment) {
            if (increment <= 0) {
                throw new IllegalArgumentException("Invalid increment " + increment);
            }
            if (start >= limit) {
                throw new IllegalArgumentException(String.format("[%d,%d) not a valid range", start, limit));
            }

            this.current = new AtomicInteger(start);
            this.limit = limit;
            this.increment = increment;
        }

        @Override
        public OfInt trySplit() {
            if (estimateSize() > 3) {
                int cur = current.get();
                int mid = cur + (increment * (int)(estimateSize() / 2));

                IntRange lower = new IntRange(cur, mid, increment);
                current.set(mid);
                return lower;
            }

            return null;
        }

        @Override
        public long estimateSize() {
            return ((limit - current.get()) / increment);
        }

        @Override
        public long getExactSizeIfKnown() {
            return estimateSize();
        }

        @Override
        public int characteristics() {
            return Spliterator.NONNULL |
                   Spliterator.IMMUTABLE |
                   Spliterator.DISTINCT |
                   Spliterator.ORDERED |
                   Spliterator.SORTED |
                   Spliterator.SIZED |
                   Spliterator.SUBSIZED;
        }

        @Override
        public Comparator<? super Integer> getComparator() {
            return Integer::compare;
        }

        @Override
        public boolean tryAdvance(IntConsumer consumer) {
            int next = current.getAndAdd(increment);
            if (next < limit) {
                consumer.accept(next);
                return true;
            }
            return false;
        }
    }

    // PRIVATE constructor to defeat instantiation.
    private ExtraStreams() {}
}
