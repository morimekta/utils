package net.morimekta.util.concurrent;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Tests for the reentrant RW mutex.
 */
public class ReentrantReadWriteMutexTest {
    private ExecutorService         executor;
    private AtomicInteger           value;
    private AtomicInteger           changes;
    private AtomicInteger           reads;
    private AtomicInteger           writes;
    private ReentrantReadWriteMutex mutex;

    @Before
    public void setUp() {
        mutex = new ReentrantReadWriteMutex();
        executor = Executors.newFixedThreadPool(10);
        value = new AtomicInteger();
        changes = new AtomicInteger();
        reads = new AtomicInteger();
        writes = new AtomicInteger();
    }

    public void assertNoChange() {
        if (reads.getAndIncrement() % 2 == 0) {
            mutex.lockForReading(() -> {
                try {
                    int val = value.get();
                    Thread.sleep(50);
                    int v2 = value.get();
                    if (val != v2) {
                        changes.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    changes.addAndGet(10000);
                }
            });
        } else {
            int v = mutex.lockForReading(() -> {
                try {
                    int val = value.get();
                    Thread.sleep(50);
                    int v2 = value.get();
                    if (val != v2) {
                        changes.incrementAndGet();
                    }
                    return v2;
                } catch (InterruptedException e) {
                    changes.addAndGet(10000);
                    return -1;
                }
            });
            assertThat(v, is(not(-1)));
        }
    }

    public void increment() {
        if (writes.getAndIncrement() % 2 == 0) {
            mutex.lockForWriting((Runnable) () -> value.incrementAndGet());
        } else {
            int v = mutex.lockForWriting(() -> value.incrementAndGet());
            assertThat(v, is(greaterThan(0)));
        }
    }

    @Test
    public void testReadWriteLock() throws InterruptedException {
        for (int i = 0; i < 100; ++i) {
            if (i % 20 == 7) {
                executor.submit(this::increment);
            } else {
                executor.submit(this::assertNoChange);
            }
            Thread.sleep(1);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(0, changes.get());
        assertEquals(5, value.get());
    }
}
