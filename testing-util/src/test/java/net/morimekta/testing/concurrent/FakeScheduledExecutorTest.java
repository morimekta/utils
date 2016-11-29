package net.morimekta.testing.concurrent;

import net.morimekta.testing.time.FakeClock;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
    @SuppressWarnings("unchecked")
    public void testInvokeAny() throws Exception {
        Callable<String> a = mock(Callable.class);
        Callable<String> b = mock(Callable.class);
        Callable<String> c = mock(Callable.class);
        Callable<String> d = mock(Callable.class);

        when(a.call()).thenThrow(new IllegalArgumentException());
        when(b.call()).thenThrow(new IllegalStateException());
        when(c.call()).thenReturn("OK");

        String result = executor.invokeAny(Arrays.asList(a, b, c, d));

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
}
