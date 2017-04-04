package net.morimekta.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Extra collector utilities.
 */
public class ExtraCollectors {
    /**
     * Collect into batches of max N items per batch. Creates a stream of lists as response.
     *
     * @param itemsPerBatch Maximum number of items per batch.
     * @param <T> The item type.
     * @return The stream of batched entries.
     */
    public static <T> Collector<T, LinkedList<List<T>>, Stream<List<T>>> inBatchesOf(int itemsPerBatch) {
        return Collector.of(// instantiation
                            LinkedList::new,
                            // accumulator
                            (l, i) -> {
                                if (l.isEmpty() || l.peekLast().size() >= itemsPerBatch) {
                                    l.add(new LinkedList<>());
                                }
                                l.peekLast().add(i);
                            },
                            // combiner
                            (a, b) -> {
                                // Merge the two lists so the batches matches the order
                                // of the non-parallel inBatchesOf with (a1..an) + (b1..bn)
                                // as the set of items. It's not extremely efficient, but
                                // works fine as this is not optimized for parallel streams.
                                while (!b.isEmpty()) {
                                    for (T i : b.peekFirst()) {
                                        if (a.peekLast().size() >= itemsPerBatch) {
                                            a.add(new LinkedList<>());
                                        }
                                        a.peekLast().add(i);
                                    }
                                    b.pollFirst();
                                }
                                return a;
                            },
                            // finalizer
                            Collection::stream);
    }

    // PRIVATE constructor to defeat instantiation.
    private ExtraCollectors() {}
}
