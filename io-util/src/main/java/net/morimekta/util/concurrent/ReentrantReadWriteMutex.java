package net.morimekta.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * A re-entrant read-write mutex wrapper.
 */
public class ReentrantReadWriteMutex implements ReadWriteMutex {
    private final Lock readLock;
    private final Lock writeLock;

    public ReentrantReadWriteMutex() {
        ReadWriteLock mutex = new ReentrantReadWriteLock();
        this.readLock = mutex.readLock();
        this.writeLock = mutex.writeLock();
    }

    @Override
    public <V> V lockForReading(Supplier<V> callable) {
        readLock.lock();
        try {
            return callable.get();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void lockForWriting(Runnable callable) {
        writeLock.lock();
        try {
            callable.run();
        } finally {
            writeLock.unlock();
        }
    }
}
