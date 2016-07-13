package net.morimekta.util.concurrent;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the reentrant RW mutex.
 */
public class ReentrantReadWriteMutexTest {
    private ExecutorService executor;
    private AtomicInteger value;
    private AtomicInteger changes;
    private ReentrantReadWriteMutex mutex;

    @Before
    public void setUp() {
        mutex = new ReentrantReadWriteMutex();
        executor = Executors.newFixedThreadPool(10);
        value = new AtomicInteger();
        changes = new AtomicInteger();
    }

    public void assertNoChange() {
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
            return null;
        });
    }

    public void increment() {
        mutex.lockForWriting(value::incrementAndGet);
    }

    @Test
    public void testReadWriteLock() throws InterruptedException {
        for (int i = 0; i < 100; ++i) {
            if (i % 20 == 7) {
                executor.submit(this::increment);
            } else {
                executor.submit(this::assertNoChange);
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        assertEquals(0, changes.get());
        assertEquals(5, value.get());
    }
}
