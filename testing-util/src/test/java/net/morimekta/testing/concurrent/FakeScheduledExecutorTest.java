package net.morimekta.testing.concurrent;

import com.google.common.collect.ImmutableList;
import net.morimekta.testing.time.FakeClock;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class FakeScheduledExecutorTest {
    private FakeClock             clock;
    private FakeScheduledExecutor executor;

    @Before
    public void setUp() {
        clock = new FakeClock();
        executor = new FakeScheduledExecutor(clock);
    }

    @Test
    public void testSchedule() throws Exception {
        Callable r1 = mock(Callable.class);
        Runnable r2 = mock(Runnable.class);
        Callable r3 = mock(Callable.class);

        when(r1.call()).thenReturn(1L);
        when(r3.call()).thenReturn(3L);

        Future f1 = executor.schedule(r1, 10L, TimeUnit.MILLISECONDS);
        Future f2 = executor.schedule(r2, 10L, TimeUnit.SECONDS);
        Future f3 = executor.schedule(r3, 10L, TimeUnit.MINUTES);

        assertThat(f1.isDone(), is(false));
        assertThat(f2.isDone(), is(false));
        assertThat(f3.isDone(), is(false));

        clock.tick(100L);

        assertThat(f1.isDone(), is(true));
        assertThat(f2.isDone(), is(false));
        assertThat(f3.isDone(), is(false));

        assertThat(f1.get(), equalTo(1L));

        clock.tick(100L, TimeUnit.SECONDS);

        assertThat(f1.isDone(), is(true));
        assertThat(f2.isDone(), is(true));
        assertThat(f3.isDone(), is(false));

        assertThat(f2.get(), nullValue());

        clock.tick(100L, TimeUnit.MINUTES);

        assertThat(f1.isDone(), is(true));
        assertThat(f2.isDone(), is(true));
        assertThat(f3.isDone(), is(true));

        assertThat(f3.get(), equalTo(3L));

        verify(r1).call();
        verify(r2).run();
        verify(r3).call();

        verifyNoMoreInteractions(r1, r2, r3);
    }

    @Test
    public void testInvokeAll() throws Exception {
        Callable<String> a = mock(Callable.class);
        Callable<String> b = mock(Callable.class);

        when(a.call()).thenReturn("a");
        when(b.call()).thenReturn("b");

        List<Future<String>> l = executor.invokeAll(ImmutableList.of(a, b), 10L, TimeUnit.MILLISECONDS);

        clock.tick(1);

        verify(a).call();
        verify(b).call();

        assertThat(l, hasSize(2));
        assertThat(l.get(0).get(), is("a"));
        assertThat(l.get(1).get(), is("b"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeAny() throws Exception {
        Callable<String> a = mock(Callable.class);
        Callable<String> b = mock(Callable.class);
        Callable<String> c = mock(Callable.class);
        Callable<String> d = mock(Callable.class);

        when(a.call()).thenThrow(new IllegalArgumentException());
        when(b.call()).thenThrow(new IllegalStateException());
        when(c.call()).thenReturn("OK");

        String result = executor.invokeAny(Arrays.asList(a, b, c, d), 10, TimeUnit.MILLISECONDS);

        assertThat(result, is(equalTo("OK")));

        verify(a).call();
        verify(b).call();
        verify(c).call();
        verifyNoMoreInteractions(a, b, c, d);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInvokeAny_failed() throws Exception {
        Callable<String> a = mock(Callable.class);
        Callable<String> b = mock(Callable.class);

        when(a.call()).thenThrow(new IllegalArgumentException());
        when(b.call()).thenThrow(new IllegalStateException());

        try {
            executor.invokeAny(Arrays.asList(a, b));
            fail("No exception");
        } catch (ExecutionException e) {
            assertThat(e.getMessage(), is(equalTo("All 2 tasks failed, first exception")));
            assertThat(e.getCause(), is(instanceOf(IllegalArgumentException.class)));
            assertThat(e.getSuppressed().length, is(1));
        }
    }

    @Test
    public void testFakeTask() throws Exception {
        ScheduledFuture<String> task = executor.schedule(() -> "a", 1000, TimeUnit.MILLISECONDS);
        assertThat(task.getDelay(TimeUnit.MILLISECONDS), is(1000L));
        assertThat(task.get(), is("a"));
        assertThat(task.get(100L, TimeUnit.MILLISECONDS), is("a"));
        clock.tick(100L);
        assertThat(task.getDelay(TimeUnit.MILLISECONDS), is(0L));

        ScheduledFuture<String> task2 = executor.schedule(() -> "a", 1000, TimeUnit.SECONDS);
        try {
            task2.get(1000L, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (TimeoutException e) {
            assertThat(e.getMessage(), is("Timed out after 1000 millis"));
        }

        assertThat(task2.cancel(true), is(true));
        assertThat(task2.isCancelled(), is(true));
        assertThat(task2.isDone(), is(true));
        try {
            task2.get(1000L, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (InterruptedException e) {
            assertThat(e.getMessage(), is("Task cancelled"));
        }

        try {
            task2.get();
            fail("no exception");
        } catch (InterruptedException e) {
            assertThat(e.getMessage(), is("Task cancelled"));
        }

        Callable<String> runnable = mock(Callable.class);
        doThrow(new IOException("Boo")).when(runnable).call();

        ScheduledFuture<String> task3 = executor.schedule(runnable, 1000, TimeUnit.MILLISECONDS);
        clock.tick(2000, TimeUnit.MILLISECONDS);

        assertThat(task3.isDone(), is(true));
        assertThat(task3.isCancelled(), is(false));

        try {
            task3.get();
            fail("no exception");
        } catch (ExecutionException e) {
            assertThat(e.getMessage(), is("Boo"));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), is("Boo"));
        }

        try {
            task3.get(100L, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (ExecutionException e) {
            assertThat(e.getMessage(), is("Boo"));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), is("Boo"));
        }

        assertThat(task, is(sameInstance(task)));
        assertThat(task, is(task));
        assertThat(task, is(not(task2)));
        assertThat(task.hashCode(), is(not(task2.hashCode())));
    }

    @Test
    public void testIllegalArgs() throws InterruptedException, ExecutionException, TimeoutException {
        Callable callable = () -> null;
        Runnable runnable = () -> {};

        try {
            executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Shutdown not triggered"));
        }

        try {
            executor.schedule(callable, -100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Unable to schedule tasks in the past"));
        }

        try {
            executor.scheduleAtFixedRate(runnable, -100, 100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid initial delay or period: -100 / 100"));
        }

        try {
            executor.scheduleAtFixedRate(runnable, 100, -100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid initial delay or period: 100 / -100"));
        }

        try {
            executor.scheduleWithFixedDelay(runnable, -100, 100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid initial delay or intermediate delay: -100 / 100"));
        }

        try {
            executor.scheduleWithFixedDelay(runnable, 100, -100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid initial delay or intermediate delay: 100 / -100"));
        }

        try {
            executor.invokeAll(ImmutableList.of(), -3, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Negative timeout: -3"));
        }

        try {
            executor.invokeAny(ImmutableList.of(), -3, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Negative timeout: -3"));
        }

        try {
            executor.invokeAll(ImmutableList.of());
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Empty invoke collection"));
        }

        try {
            executor.invokeAny(ImmutableList.of());
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Empty invoke collection"));
        }
    }

    @Test
    public void testShutdown() throws Exception {
        Callable<String> callable = () -> null;
        Runnable runnable = () -> {};

        executor.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        executor.shutdown();

        assertThat(executor.isTerminated(), is(false));

        try {
            executor.schedule(callable, 100, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.invokeAny(ImmutableList.of(callable));
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.invokeAll(ImmutableList.of(callable));
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.scheduleWithFixedDelay(runnable, 10, 10, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.scheduleAtFixedRate(runnable, 10, 10, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        assertThat(executor.shutdownNow(), hasSize(1));
        assertThat(executor.isTerminated(), is(true));
    }

    @Test
    public void testAwaitTermination_immediate() throws InterruptedException {
        AtomicInteger calls = new AtomicInteger();
        Runnable runnable = calls::incrementAndGet;

        executor.execute(runnable);
        executor.submit(calls::incrementAndGet);
        executor.submit(runnable);
        executor.submit(runnable, "");
        executor.shutdown();

        assertThat(executor.isTerminated(), is(false));
        assertThat(executor.awaitTermination(10, TimeUnit.MILLISECONDS), is(true));
        assertThat(calls.get(), is(4));
        assertThat(executor.isTerminated(), is(true));
        assertThat(executor.shutdownNow(), hasSize(0));
    }

    @Test
    public void testAwaitTermination() throws InterruptedException {
        AtomicInteger calls = new AtomicInteger();
        Runnable runnable = calls::incrementAndGet;

        executor.submit(calls::incrementAndGet);
        executor.submit(runnable);
        executor.submit(runnable, "");
        executor.schedule(runnable, 10, TimeUnit.MILLISECONDS);
        executor.schedule(runnable, 100, TimeUnit.MILLISECONDS);
        executor.schedule(runnable, 1000, TimeUnit.MILLISECONDS);
        executor.schedule(runnable, 10000, TimeUnit.MILLISECONDS);
        executor.shutdown();

        assertThat(executor.isTerminated(), is(false));
        assertThat(executor.awaitTermination(5000, TimeUnit.MILLISECONDS), is(false));

        assertThat(calls.get(), is(6));

        assertThat(executor.isTerminated(), is(false));
        assertThat(executor.shutdownNow(), hasSize(1));
        assertThat(executor.isTerminated(), is(true));
    }

    @Test
    public void testScheduledAtFixedRate() throws ExecutionException {
        AtomicInteger calls = new AtomicInteger();
        Runnable task = () -> {
            calls.incrementAndGet();
            clock.tick(10);
        };

        Future rate = executor.scheduleAtFixedRate(task, 100, 50, TimeUnit.MILLISECONDS);

        clock.tick(210);

        assertThat(calls.get(), is(3));

        rate.cancel(true);

        assertThat(rate.isCancelled(), is(true));

        clock.tick(100000L);

        assertThat(calls.get(), is(3));

        try {
            rate.get();
            fail("no exception");
        } catch (InterruptedException e) {
            assertThat(e.getMessage(), is("Task cancelled"));
        }
    }

    @Test
    public void testScheduledAtFixedDelay() {
        AtomicInteger calls = new AtomicInteger();
        Runnable task = () -> {
            calls.incrementAndGet();
            clock.tick(10);
        };

        Future rate = executor.scheduleWithFixedDelay(task, 100, 50, TimeUnit.MILLISECONDS);
        Future other = executor.schedule(() -> null, 10, TimeUnit.MILLISECONDS);

        clock.tick(210);

        assertThat(calls.get(), is(2));

        assertThat(rate, is(not(other)));
    }
}
