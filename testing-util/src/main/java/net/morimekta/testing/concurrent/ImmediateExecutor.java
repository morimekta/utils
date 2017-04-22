package net.morimekta.testing.concurrent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Fake executor service that runs all tasks immediately. If you need a fake
 * executor that delays execution, use {@link FakeScheduledExecutor}, and
 * trigger executions by ticking the clock.
 */
public class ImmediateExecutor implements ExecutorService {
    private static class DoneTask<V> implements Future<V> {
        private final V result;
        private final Throwable except;

        private DoneTask(V result, Exception e) {
            this.result = result;
            this.except = e;
        }

        @Override
        public boolean cancel(boolean b) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            if (except != null) {
                throw new ExecutionException(except.getMessage(), except);
            }
            return result;
        }

        @Override
        public V get(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }
    }

    private boolean shutdownCalled = false;

    public ImmediateExecutor() {}

    @Override
    public void shutdown() {
        this.shutdownCalled = true;
    }

    @Override @Nonnull
    @SuppressWarnings("unchecked")
    public List<Runnable> shutdownNow() {
        this.shutdownCalled = true;
        return Collections.EMPTY_LIST;
    }

    @Override
    public boolean isShutdown() {
        return shutdownCalled;
    }

    @Override
    public boolean isTerminated() {
        return shutdownCalled;
    }

    @Override
    public boolean awaitTermination(long l, @Nonnull TimeUnit timeUnit) throws InterruptedException {
        if (!shutdownCalled) {
            throw new IllegalStateException("Shutdown not triggered");
        }
        return true;
    }

    @Override @Nonnull
    public <T> DoneTask<T> submit(@Nonnull Callable<T> callable) {
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down");
        }
        try {
            return new DoneTask<>(callable.call(), null);
        } catch (Exception e) {
            return new DoneTask<>(null, e);
        }
    }

    @Override @Nonnull
    public <T> DoneTask<T> submit(@Nonnull Runnable runnable, T t) {
        if (isShutdown()) {
            throw new IllegalStateException("Executor is shut down");
        }
        try {
            runnable.run();
        } catch (Exception e) {
            return new DoneTask<>(null, e);
        }
        return new DoneTask<>(t, null);
    }

    @Override @Nonnull
    public DoneTask<?> submit(@Nonnull Runnable runnable) {
        return submit(runnable, null);
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
        submit(runnable, null);
    }
}
