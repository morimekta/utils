package net.morimekta.testing.concurrent;

import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class ImmediateExecutorTest {
    @Test
    public void testExecute() throws ExecutionException, InterruptedException {
        ImmediateExecutor executor = new ImmediateExecutor();

        Runnable runnable = mock(Runnable.class);

        Future result = executor.submit(runnable);

        verify(runnable).run();
        verifyNoMoreInteractions(runnable);

        assertThat(result.isDone(), is(true));
        assertThat(result.get(), nullValue());
    }
}
