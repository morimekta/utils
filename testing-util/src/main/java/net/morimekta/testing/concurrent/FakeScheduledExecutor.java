package net.morimekta.testing.concurrent;

import net.morimekta.testing.time.FakeClock;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * A scheduled executor that uses a fake clock as back-bone to the executor.
 * To trigger the executions, call {@link FakeClock#tick(long)} on the fake
 * clock.
 */
public class FakeScheduledExecutor implements ScheduledExecutorService, FakeClock.TimeListener {
    private class FakeTask<V> implements ScheduledFuture<V>, Runnable {
        private final long triggersAtMs;
        private final Callable<V> callable;
        private final int id;
        private V result;
        private Throwable except;

        private boolean cancelled;
        private boolean done;

        private FakeTask(long triggersAtMs,
                         Callable<V> callable) {
            this.triggersAtMs = triggersAtMs;
            this.id = nextId.incrementAndGet();
            this.callable = callable;

            this.cancelled = false;
            this.done = false;
            this.result = null;
            this.except = null;
        }

        @Override
        public int hashCode() {
            return Objects.hash(FakeTask.class, triggersAtMs, id);
        }

        @Override
        public int compareTo(@Nonnull Delayed delayed) {
            if (delayed instanceof FakeTask) {
                int c = Long.compare(triggersAtMs, ((FakeTask) delayed).triggersAtMs);
                if (c == 0) {
                    return Integer.compare(id, ((FakeTask) delayed).id);
                }
                return c;
            }
            return -1;
        }

        @Override
        public long getDelay(@Nonnull TimeUnit timeUnit) {
            long now = clock.millis();
            if (now < triggersAtMs) {
                return timeUnit.convert(triggersAtMs - now, TimeUnit.MILLISECONDS);
            }
            return 0L;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!done) {
                cancelled = true;
                done = true;
                scheduledTasks.remove(this);
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
                clock.tick(getDelay(TimeUnit.MILLISECONDS));
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
                long maxWait = timeUnit.toMillis(l);
                clock.tick(Math.min(maxWait, getDelay(TimeUnit.MILLISECONDS)));
                if (!done) {
                    throw new TimeoutException("Timed out after " + timeUnit.toMillis(l) + " millis");
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
            try {
                result = callable.call();
            } catch (Exception e) {
                this.except = e;
            }
            this.result = result;
            this.done = true;
        }
    }

    private class FakeRecurringTask implements ScheduledFuture<Void> {
        private final long delay;
        private final Runnable callable;
        private final AtomicReference<FakeTask> next;
        AtomicLong nextExecution;

        private boolean cancelled;

        private FakeRecurringTask(long delay,
                                  long initialDelay,
                                  TimeUnit timeUnit,
                                  Runnable callable,
                                  AtomicReference<FakeTask> first) {
            this.delay = timeUnit.toMillis(delay);
            this.nextExecution = new AtomicLong(clock.millis() + timeUnit.toMillis(initialDelay));
            this.callable = callable;
            this.next = first;
            this.cancelled = false;
        }

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int compareTo(@Nonnull Delayed delayed) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS),
                                delayed.getDelay(TimeUnit.MILLISECONDS));
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!cancelled) {
                cancelled = true;
                scheduledTasks.remove(next.get());
            }
            return cancelled;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public boolean isDone() {
            return cancelled;
        }

        @Override
        public Void get() throws InterruptedException, ExecutionException {
            if (cancelled) {
                throw new InterruptedException("Task cancelled");
            }
            throw new IllegalStateException("Cannot wait for fake recurring tasks");
        }

        @Override
        public Void get(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }

        @Override
        public long getDelay(@Nonnull TimeUnit timeUnit) {
            return timeUnit.convert(delay, TimeUnit.MILLISECONDS);
        }

        void runWithDelay() {
            try {
                callable.run();
            } catch (Exception e) {
                // e.printStackTrace();
                // keep going.
            }
            next.set(schedule(this::runWithDelay, delay, TimeUnit.MILLISECONDS));
        }

        void runWithRate() {
            long now = clock.millis();

            // Run the task for each time it should have been run in the
            // time the last 'tick' should have triggered.
            long nextRun = nextExecution.get();
            while (nextRun <= now) {
                try {
                    callable.run();
                } catch (Exception e) {
                    // e.printStackTrace();
                    // keep going.
                }
                nextRun = nextExecution.addAndGet(delay);
            }
            long delay = nextRun - now;

            next.set(schedule(this::runWithRate, delay, TimeUnit.MILLISECONDS));
        }
    }

    private final FakeClock         clock;
    private final HashSet<FakeTask> scheduledTasks;
    private final AtomicInteger     nextId = new AtomicInteger();

    private boolean shutdownCalled = false;

    public FakeScheduledExecutor(@Nonnull FakeClock clock) {
        this.scheduledTasks = new HashSet<>();
        this.clock = clock;
        this.clock.addListener(this);
    }

    @Override
    public void newCurrentTimeUTC(long millis) {
        for (FakeTask<?> task : new TreeSet<>(scheduledTasks)) {
            if (task.triggersAtMs <= millis) {
                task.run();
                scheduledTasks.remove(task);
            }
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
            throw new IllegalStateException("Executor is shut down");
        }
        if (delay < 0) {
            throw new IllegalArgumentException("Unable to schedule tasks in the past");
        }

        long now = clock.millis();
        long triggersAtMs = timeUnit.toMillis(delay) + now;
        FakeTask<V> task = new FakeTask<>(triggersAtMs, callable);
        scheduledTasks.add(task);
        return task;
    }

    @Override @Nonnull
    public FakeRecurringTask scheduleAtFixedRate(@Nonnull Runnable runnable, long initialDelay, long period, @Nonnull TimeUnit timeUnit) {
        if (initialDelay < 0 || period < 1) {
            throw new IllegalArgumentException("Invalid initial delay or period: " + initialDelay + " / " + period);
        }
        AtomicReference<FakeTask> first = new AtomicReference<>();
        FakeRecurringTask recurring = new FakeRecurringTask(period, initialDelay, timeUnit, runnable, first);
        first.set(schedule(recurring::runWithRate, initialDelay, timeUnit));
        return recurring;
    }

    @Override @Nonnull
    public FakeRecurringTask scheduleWithFixedDelay(@Nonnull Runnable runnable, long initialDelay, long delay, @Nonnull TimeUnit timeUnit) {
        if (initialDelay < 0 || delay < 1) {
            throw new IllegalArgumentException("Invalid initial delay or intermediate delay: " + initialDelay + " / " + delay);
        }
        AtomicReference<FakeTask> first = new AtomicReference<>();
        FakeRecurringTask recurring = new FakeRecurringTask(delay, initialDelay, timeUnit, runnable, first);
        first.set(schedule(recurring::runWithDelay, initialDelay, timeUnit));
        return recurring;
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
            throw new IllegalStateException("Shutdown not triggered");
        }
        long untilTotal = timeUnit.toMillis(l) + clock.millis();

        while (!scheduledTasks.isEmpty()) {
            FakeTask<?> next = new TreeSet<>(scheduledTasks).first();

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
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty invoke collection");
        }

        List<Future<T>> results = new ArrayList<>();
        for (Callable<T> c : collection) {
            results.add(submit(c));
        }
        return results;
    }

    @Override @Nonnull
    public <T> List<Future<T>> invokeAll(
            @Nonnull Collection<? extends Callable<T>> collection,
            long l,
            @Nonnull TimeUnit timeUnit) throws InterruptedException {
        if (l < 0) {
            throw new IllegalArgumentException("Negative timeout: " + l);
        }
        return invokeAll(collection);
    }

    @Override @Nonnull
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down");
        }
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty invoke collection");
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
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> collection,
                           long l,
                           @Nonnull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        if (l < 0) {
            throw new IllegalArgumentException("Negative timeout: " + l);
        }
        return invokeAny(collection);
    }

    @Override
    public void execute(@Nonnull Runnable runnable) {
        schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }
}
