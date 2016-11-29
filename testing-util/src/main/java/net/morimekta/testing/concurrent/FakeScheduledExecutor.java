package net.morimekta.testing.concurrent;

import net.morimekta.testing.time.FakeClock;

import javax.annotation.Nonnull;
import java.time.Clock;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A scheduled executor that uses a fake clock as back-bone to the executor.
 * To trigger the executions, call {@link FakeClock#tick(long)} on the fake
 * clock.
 */
public class FakeScheduledExecutor implements ScheduledExecutorService, FakeClock.TimeListener {
    private static class FakeTask<V> implements ScheduledFuture<V>, Runnable {
        private final long triggersAtMs;
        private final Callable<V> callable;
        private final Clock clock;
        private V result;
        private Throwable except;

        private boolean cancelled;
        private boolean done;

        private FakeTask(long triggersAtMs,
                         Clock clock,
                         Callable<V> callable) {
            this.triggersAtMs = triggersAtMs;
            this.clock = clock;
            this.callable = callable;

            this.cancelled = false;
            this.done = false;
            this.result = null;
            this.except = null;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return Objects.hash(FakeTask.class, clock, callable, triggersAtMs);
        }

        @Override
        public int compareTo(@Nonnull Delayed delayed) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), delayed.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public long getDelay(@Nonnull TimeUnit timeUnit) {
            long now = clock.millis();
            return timeUnit.convert(triggersAtMs - now, TimeUnit.MILLISECONDS);
        }

        @Override
        public boolean cancel(boolean b) {
            if (!done) {
                cancelled = true;
                done = true;
            }
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            if (!done) {
                this.wait();
            }
            if (cancelled) {
                throw new InterruptedException("Task cancelled");
            }
            if (except != null) {
                throw new ExecutionException(except.getMessage(), except);
            }
            return result;
        }

        @Override
        public V get(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            if (!done) {
                this.wait(timeUnit.toMillis(l));
                if (!done) {
                    throw new TimeoutException("Timed out after " + timeUnit.toMillis(l) + " millis.");
                }
            }
            if (cancelled) {
                throw new InterruptedException("Task cancelled");
            }
            if (except != null) {
                throw new ExecutionException(except.getMessage(), except);
            }
            return result;
        }

        @Override
        public void run() {
            V result = null;
            if (isDone()) {
                return;
            }
            try {
                result = callable.call();
            } catch (Exception e) {
                this.except = e;
            }
            this.result = result;
            this.done = true;
        }
    }

    private final FakeClock         clock;
    private final TreeSet<FakeTask> scheduledTasks;

    private boolean shutdownCalled = false;

    public FakeScheduledExecutor(@Nonnull FakeClock clock) {
        this.scheduledTasks = new TreeSet<>();
        this.clock = clock;
        this.clock.addListener(this);
    }

    @Override
    public void newCurrentTimeUTC(long millis) {
        while (!scheduledTasks.isEmpty()) {
            FakeTask<?> first = scheduledTasks.first();
            if (first.getDelay(TimeUnit.MILLISECONDS) > 0) {
                break;
            }

            first.run();
            scheduledTasks.remove(first);
        }
    }

    @Override @Nonnull
    public FakeTask<?> schedule(@Nonnull Runnable runnable, long l, @Nonnull TimeUnit timeUnit) {
        return this.schedule(() -> {
            runnable.run();
            return null;
        }, l, timeUnit);
    }

    @Override @Nonnull
    public <V> FakeTask<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit timeUnit) {
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down.");
        }

        long now = clock.millis();
        long triggersAtMs = timeUnit.toMillis(delay) + now;
        if (now > triggersAtMs) {
            throw new IllegalArgumentException("Unable to schedule tasks in the past");
        }
        FakeTask<V> task = new FakeTask<>(triggersAtMs, clock, callable);
        scheduledTasks.add(task);
        return task;
    }

    @Override @Nonnull
    public FakeTask<?> scheduleAtFixedRate(@Nonnull Runnable runnable, long initialDelay, long period, @Nonnull TimeUnit timeUnit) {
        if (initialDelay < 0 || period < 1) {
            throw new IllegalArgumentException("Invalid initial delay or period: " + initialDelay + " / " + period);
        }
        AtomicReference<Runnable> ref = new AtomicReference<>();
        AtomicLong nextExecution = new AtomicLong(clock.millis() + timeUnit.toMillis(initialDelay));
        ref.set(() -> {
            long now = clock.millis();

            // Run the task for each time it should have been run in the
            // time the last 'tick' should have triggered.
            long next = nextExecution.get();
            while (next <= now) {
                try {
                    runnable.run();
                } catch (Exception e) {
                    // e.printStackTrace();
                    // keep going.
                }
                next = nextExecution.addAndGet(timeUnit.toMillis(period));
            }
            long delay = next - now;
            schedule(ref.get(), delay, TimeUnit.MILLISECONDS);
        });
        return schedule(ref.get(), initialDelay, timeUnit);
    }

    @Override @Nonnull
    public FakeTask<?> scheduleWithFixedDelay(@Nonnull Runnable runnable, long initialDelay, long delay, @Nonnull TimeUnit timeUnit) {
        if (initialDelay < 0 || delay < 1) {
            throw new IllegalArgumentException("Invalid initial delay or intermediate delay: " + initialDelay + " / " + delay);
        }
        AtomicReference<Runnable> ref = new AtomicReference<>();
        ref.set(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                // e.printStackTrace();
                // keep going.
            }
            schedule(ref.get(), delay, timeUnit);
        });
        return schedule(ref.get(), initialDelay, timeUnit);
    }

    @Override
    public void shutdown() {
        this.shutdownCalled = true;
    }

    @Override @Nonnull
    public List<Runnable> shutdownNow() {
        this.shutdownCalled = true;
        List<Runnable> result = scheduledTasks.stream()
                .filter(t -> !t.isCancelled())
                .collect(Collectors.toList());
        scheduledTasks.clear();
        return result;
    }

    @Override
    public boolean isShutdown() {
        return shutdownCalled;
    }

    @Override
    public boolean isTerminated() {
        return shutdownCalled && scheduledTasks.isEmpty();
    }

    @Override
    public boolean awaitTermination(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException {
        if (!shutdownCalled) {
            throw new IllegalStateException("Shutdown not triggered.");
        }
        long untilTotal = timeUnit.toMillis(l) + clock.millis();

        while (!scheduledTasks.isEmpty()) {
            FakeTask<?> next = scheduledTasks.first();

            long now   = clock.millis();
            long delay = next.getDelay(TimeUnit.MILLISECONDS);
            if (delay > 0 && (now + delay) > untilTotal) {
                break;
            }

            if (delay > 0) {
                clock.tick(delay);
            } else {
                newCurrentTimeUTC(now);
            }
        }

        return scheduledTasks.isEmpty();
    }

    @Override @Nonnull
    public <T> FakeTask<T> submit(@Nonnull Callable<T> callable) {
        return schedule(callable, 0, TimeUnit.MILLISECONDS);
    }

    @Override @Nonnull
    public <T> FakeTask<T> submit(@Nonnull Runnable runnable, T t) {
        return schedule(() -> {
            runnable.run();
            return t;
        }, 0, TimeUnit.MILLISECONDS);
    }

    @Override @Nonnull
    public FakeTask<?> submit(@Nonnull Runnable runnable) {
        return schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    @Override @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> collection) throws InterruptedException {
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down");
        }

        List<Future<T>> results = new LinkedList<>();
        for (Callable<T> c : collection) {
            results.add(submit(c));
        }
        return results;
    }

    @Override @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> collection, long l, @Nonnull
            TimeUnit timeUnit) throws InterruptedException {
        return invokeAll(collection);
    }

    @Override @Nonnull
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty invoke collection");
        }
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down");
        }

        ExecutionException ex = null;
        for (Callable<T> c : collection) {
            try {
                return c.call();
            } catch (Exception e) {
                if (ex == null) {
                    ex = new ExecutionException("All " + collection.size() + " tasks failed, first exception", e);
                } else {
                    ex.addSuppressed(e);
                }
            }
        }
        throw ex;
    }

    @Override
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection, long l, @Nonnull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        return invokeAny(collection);
    }

    @Override
    public void execute(@Nonnull Runnable runnable) {
        schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }
}
