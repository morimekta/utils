package net.morimekta.util.concurrent;

import java.util.function.Supplier;

/**
 * Interface for wrapping a read-write muted into java-8 functional interface.
 */
public interface ReadWriteMutex {
    /**
     * Lock the config for a read operation that must be read atomic.
     *
     * @param callable The enclosed callable to be run inside read lock.
     * @param <V> The read return value.
     * @return The supplied value.
     */
    <V> V lockForReading(Supplier<V> callable);

    /**
     * Lock the config for a write operation that must be write atomic, and
     * could interfere with read operations.
     *
     * @param callable The callable operation.
     */
    void lockForWriting(Runnable callable);
}
