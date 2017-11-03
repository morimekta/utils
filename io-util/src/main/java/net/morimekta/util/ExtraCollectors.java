package net.morimekta.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
                                    l.add(new ArrayList<>(itemsPerBatch));
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
                                            a.add(new ArrayList<>(itemsPerBatch));
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

    /**
     * Collect into N batches of approximate equal size. Creates a stream of lists as response.
     *
     * @param numBatches Number of batch to split between.
     * @param <T> The item type.
     * @return The stream of batched entries.
     */
    public static <T> Collector<T, ArrayList<List<T>>, Stream<List<T>>> inNumBatches(int numBatches) {
        // Keep the position separate from the possible threads to enable parallel
        // collection.
        AtomicInteger nextPos = new AtomicInteger(0);
        AtomicInteger numTotal = new AtomicInteger(0);
        return Collector.of(// instantiation
                            () -> {
                                ArrayList<List<T>> batches = new ArrayList<>(numBatches);
                                for (int i = 0; i < numBatches; ++i) {
                                    batches.add(new ArrayList<>());
                                }
                                return batches;
                            },
                            // accumulator
                            (batches, item) -> {
                                int pos = nextPos.getAndUpdate(i -> (++i) % numBatches);
                                batches.get(pos).add(item);
                                numTotal.incrementAndGet();
                            },
                            // combiner
                            (a, b) -> {
                                // Merge the two lists so the batches matches the order
                                // of the non-parallel inBatchesOf with (a1..an) + (b1..bn)
                                // as the set of items. It's not extremely efficient, but
                                // works fine as this is not optimized for parallel streams.
                                for (int i = 0; i < numBatches; ++i) {
                                    List<T> al = a.get(i);
                                    List<T> bl = b.get(i);
                                    al.addAll(bl);
                                }
                                return a;
                            },
                            // finalizer
                            batches -> {
                                if (numTotal.get() < numBatches) {
                                    return batches.subList(0, numTotal.get()).stream();
                                }
                                return batches.stream();
                            });
    }

    // PRIVATE constructor to defeat instantiation.
    private ExtraCollectors() {}
}
