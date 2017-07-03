package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.test_utils.FakeClock;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ProgressManagerTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testSingleThread() throws IOException, InterruptedException, ExecutionException {
        try (Terminal term = new Terminal(console.tty());
             ProgressManager progress = new ProgressManager(term, Progress.Spinner.ASCII,
                                                            1)) {

            Future<String> first = progress.addTask("First", 10000, task -> {
                try {
                    Thread.sleep(60);
                    task.accept(1000);
                } catch (InterruptedException ignore) {
                }
                throw new RuntimeException("Failed");
            });
            Future<String> second = progress.addTask("Second", 10000, task -> {
                try {
                    Thread.sleep(50);
                    task.accept(1000);
                    Thread.sleep(100);
                    task.accept(10000);
                } catch (InterruptedException ignore) {
                }
                return "OK";
            });

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of()));

            Thread.sleep(25L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [--------------------------------------------------------------------------------------------------------------------]   0% |",
                                           " -- And 1 more...")));

            Thread.sleep(50L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [-------------------------------------------------------------------------------------------------------------------]   0% |")));

            Thread.sleep(100L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% |")));

            progress.waitAbortable();

            try {
                first.get();
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }
            assertThat(second.get(), is("OK"));

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###################################################################################################################] 100% v @  0.1  s")));
        }
    }

    @Test
    public void testMultiThread() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        try (Terminal term = new Terminal(console.tty());
             ProgressManager progress = new ProgressManager(term, Progress.Spinner.ASCII)) {

            Future<String> first = progress.addTask("First", 10000, task -> {
                try {
                    Thread.sleep(50);
                    task.accept(1000);
                } catch (InterruptedException ignore) {
                }
                throw new RuntimeException("Failed");
            });
            Future<String> second = progress.addTask("Second", 10000, task -> {
                try {
                    Thread.sleep(50);
                    task.accept(1000);
                    Thread.sleep(100);
                    task.accept(10000);
                } catch (InterruptedException ignore) {
                }
                return "OK";
            });

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of()));

            Thread.sleep(35L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [--------------------------------------------------------------------------------------------------------------------]   0% |",
                                           "Second: [-------------------------------------------------------------------------------------------------------------------]   0% |")));

            Thread.sleep(50L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% |")));

            progress.waitAbortable();

            try {
                first.get(10L, TimeUnit.MILLISECONDS);
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }
            assertThat(second.get(10L, TimeUnit.MILLISECONDS), is("OK"));

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###################################################################################################################] 100% v @  0.1  s")));
        }
    }
    @Test
    public void testAbort() throws IOException, InterruptedException, ExecutionException {
        console.setInput(Char.ABR);

        try (Terminal term = new Terminal(console.tty());
             ProgressManager progress = new ProgressManager(term, Progress.Spinner.ASCII)) {

            Future<String> first = progress.addTask("First", 10000, task -> {
                Thread.sleep(20);
                task.accept(1000);
                throw new RuntimeException("Failed");
            });
            Future<String> second = progress.addTask("Second", 10000, task -> {
                Thread.sleep(50);
                task.accept(1000);
                Thread.sleep(120);
                task.accept(10000);
                return "OK";
            });

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of()));

            Thread.sleep(15L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [--------------------------------------------------------------------------------------------------------------------]   0% |",
                                           "Second: [-------------------------------------------------------------------------------------------------------------------]   0% |")));

            Thread.sleep(50L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% |")));

            try {
                progress.waitAbortable();
                fail("No exception");
            } catch (IOException e) {
                assertThat(e.getMessage(), is("Aborted with '<ABR>'"));
            }

            try {
                first.get();
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }
            try {
                String s = second.get();
                fail("No exception: " + s);
            } catch (CancellationException e) {
                assertThat(e.getMessage(), is("Cancelled"));
            }

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% Cancelled")));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInternalTask() {
        FakeClock clock = new FakeClock();
        ExecutorService executor = mock(ExecutorService.class);
        Runnable after = mock(Runnable.class);
        Future<String> future = mock(Future.class);

        ProgressManager.ProgressHandler<String> handler = mock(ProgressManager.ProgressHandler.class);
        ProgressManager.InternalTask<String> task = new ProgressManager.InternalTask<>(clock, "Title", 1234, handler);

        when(executor.submit((Callable<String>) isNotNull())).thenReturn(future);

        task.start(executor, after);

        verify(executor).submit((Callable<String>) isNotNull());
        verifyNoMoreInteractions(executor, after, handler, future);
        reset(executor, after, handler, future);

        assertThat(task.isCancelled(), is(false));
        assertThat(task.isDone(), is(false));

        task.close();

        assertThat(task.isCancelled(), is(true));
        assertThat(task.isDone(), is(true));
    }

    private static List<String> stripNonPrintableLines(List<String> lines) {
        return ImmutableList.copyOf(lines.stream().map(CharUtil::stripNonPrintable).collect(Collectors.toList()));
    }
}
