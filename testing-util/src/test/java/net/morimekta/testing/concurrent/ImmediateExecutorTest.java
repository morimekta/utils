package net.morimekta.testing.concurrent;

import com.google.common.collect.ImmutableList;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class ImmediateExecutorTest {
    private ImmediateExecutor executor = new ImmediateExecutor();
    @Test
    public void testExecute() throws ExecutionException, InterruptedException {
        Runnable runnable = mock(Runnable.class);
        Runnable executed = mock(Runnable.class);

        Future result = executor.submit(runnable);
        executor.execute(executed);

        verify(executed).run();
        verifyNoMoreInteractions(executed);
        verify(runnable).run();
        verifyNoMoreInteractions(runnable);

        assertThat(result.cancel(true), is(false));
        assertThat(result.isCancelled(), is(false));
        assertThat(result.isDone(), is(true));
        assertThat(result.get(), nullValue());
    }

    @Test
    public void testDoneTask() throws Exception {
        Callable<String> callable = mock(Callable.class);

        when(callable.call()).thenReturn("a");

        Future<String> task = executor.submit(callable);

        assertThat(task.get(), is("a"));
        assertThat(task.isDone(), is(true));
    }

    @Test
    public void testDoneTask_excepted() throws Exception {
        Callable<String> callable = mock(Callable.class);

        when(callable.call()).thenThrow(new IOException("e"));

        Future<String> task = executor.submit(callable);

        assertThat(task.isDone(), is(true));
        try {
            task.get(1, TimeUnit.MILLISECONDS);
            fail("no exception");
        } catch (ExecutionException e) {
            assertThat(e.getMessage(), is("e"));
            assertThat(e.getCause(), instanceOf(IOException.class));
            assertThat(e.getCause().getMessage(), is("e"));
        }
    }

    @Test
    public void testDoneTask_excepted2() throws Exception {
        Runnable callable = mock(Runnable.class);

        doThrow(new RuntimeException("e")).when(callable).run();

        Future task = executor.submit(callable);

        assertThat(task.isDone(), is(true));
        try {
            task.get();
            fail("no exception");
        } catch (ExecutionException e) {
            assertThat(e.getMessage(), is("e"));
            assertThat(e.getCause(), instanceOf(RuntimeException.class));
            assertThat(e.getCause().getMessage(), is("e"));
        }
    }

    @Test
    public void testShutdown() throws InterruptedException, TimeoutException, ExecutionException {
        try {
            executor.awaitTermination(1, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Shutdown not triggered"));
        }

        executor.shutdown();

        assertThat(executor.isShutdown(), is(true));
        assertThat(executor.isTerminated(), is(true));
        assertThat(executor.awaitTermination(1, TimeUnit.MILLISECONDS), is(true));
        assertThat(executor.shutdownNow(), hasSize(0));

        try {
            executor.submit(() -> {});
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.submit(() -> null);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.submit(() -> {}, "");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.execute(() -> {});
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            executor.invokeAll(ImmutableList.of(() -> null), 1, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }

        try {
            String s = executor.invokeAny(ImmutableList.of(() -> ""), 1L, TimeUnit.MILLISECONDS);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Executor is shut down"));
        }
    }

    @Test
    public void testInvokeAll() throws Exception {
        Callable<String> a = mock(Callable.class);
        Callable<String> b = mock(Callable.class);

        when(a.call()).thenReturn("a");
        when(b.call()).thenReturn("b");

        List<Future<String>> l = executor.invokeAll(ImmutableList.of(a, b), 10L, TimeUnit.MILLISECONDS);

        verify(a).call();
        verify(b).call();

        Assert.assertThat(l, hasSize(2));
        Assert.assertThat(l.get(0).get(), CoreMatchers.is("a"));
        Assert.assertThat(l.get(1).get(), CoreMatchers.is("b"));
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

        Assert.assertThat(result, CoreMatchers.is(equalTo("OK")));

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
            Assert.assertThat(e.getMessage(), CoreMatchers.is(equalTo("All 2 tasks failed, first exception")));
            Assert.assertThat(e.getCause(), CoreMatchers.is(CoreMatchers.instanceOf(IllegalArgumentException.class)));
            Assert.assertThat(e.getSuppressed().length, CoreMatchers.is(1));
        }
    }
}
