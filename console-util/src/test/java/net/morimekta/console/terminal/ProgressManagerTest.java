package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.test_utils.FakeClock;
import net.morimekta.console.test_utils.FakeScheduledExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ProgressManagerTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    private FakeClock clock;
    private FakeScheduledExecutor executor;
    private Terminal terminal;

    @Before
    public void setUp() {
        clock = new FakeClock();
        executor = new FakeScheduledExecutor(clock);
        terminal = new Terminal(console.tty()) {
            @Override
            protected void sleep(long millis) throws InterruptedException {
                clock.tick(millis);
            }
        };
    }

    @After
    public void tearDown() throws IOException {
        terminal.close();
    }

    @Test
    public void testSingleThread() throws IOException, InterruptedException, ExecutionException {
        ArrayList<ProgressManager.InternalTask<String>> started = new ArrayList<>();

        try (ProgressManager progress = new ProgressManager(terminal, Progress.Spinner.ASCII,
                                                            1,
                                                            executor,
                                                            clock)) {

            Future<String> first = progress.addTask("First", 10000,
                                                    (a, b) -> started.add((ProgressManager.InternalTask<String>) a));
            Future<String> second = progress.addTask("Second", 10000,
                                                     (a, b) -> started.add((ProgressManager.InternalTask<String>) a));

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of()));

            clock.tick(250L);  // does the render

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [--------------------------------------------------------------------------------------------------------------------]   0% |",
                                           " -- And 1 more...")));

            assertThat(started, hasSize(1));

            started.get(0).accept(1000);

            clock.tick(250L);  // does the render

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% /",
                                           " -- And 1 more...")));

            started.get(0).completeExceptionally(new Exception("Failed"));

            clock.tick(250L);  // does the render

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [-------------------------------------------------------------------------------------------------------------------]   0% |")));

            assertThat(started, hasSize(2));

            started.get(1).accept(1000);

            clock.tick(250L);  // does the render

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% /")));

            started.get(1).complete("OK");

            clock.tick(250L);  // does the render

            try {
                first.get();
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }
            assertThat(second.get(), is("OK"));

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###################################################################################################################] 100% v @  0.5  s")));
        }
    }

    @Test
    public void testMultiThread() throws IOException, InterruptedException, ExecutionException, TimeoutException {
        try (ProgressManager progress = new ProgressManager(terminal, Progress.Spinner.ASCII)) {

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
                    Thread.sleep(150);
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

            Thread.sleep(100L);

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% |")));

            progress.waitAbortable();

            try {
                first.get(10L, MILLISECONDS);
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }
            assertThat(second.get(10L, MILLISECONDS), is("OK"));

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###################################################################################################################] 100% v @  0.2  s")));
        }
    }
    @Test
    public void testAbort() throws IOException, InterruptedException, ExecutionException {
        console.setInput(Char.ABR);

        try (ProgressManager progress = new ProgressManager(terminal, Progress.Spinner.ASCII)) {

            Future<String> first = progress.addTask("First", 10000, task -> {
                Thread.sleep(20);
                task.accept(1000);
                throw new RuntimeException("Failed");
            });
            Future<String> second = progress.addTask("Second", 10000, task -> {
                Thread.sleep(50);
                task.accept(1000);
                Thread.sleep(520);
                task.accept(10000);
                return "OK";
            });

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of()));

            Thread.sleep(100L);

            try {
                progress.waitAbortable();
                fail("No exception");
            } catch (IOException e) {
                assertThat(e.getMessage(), is("Aborted with '<ABR>'"));
            }

            assertThat(first.isDone(), is(true));
            try {
                first.get();
                fail("No exception");
            } catch (ExecutionException e) {
                assertThat(e.getCause().getMessage(), is("Failed"));
            }

            assertThat(second.isCancelled(), is(true));
            try {
                String s = second.get();
                fail("No exception: " + s);
            } catch (CancellationException e) {
                // nothing to verify on exception.
            }

            assertThat(stripNonPrintableLines(progress.lines()),
                       is(ImmutableList.of("First: [###########---------------------------------------------------------------------------------------------------------]  10% Failed",
                                           "Second: [###########--------------------------------------------------------------------------------------------------------]  10% Cancelled")));
        }
    }

    private static List<String> stripNonPrintableLines(List<String> lines) {
        return ImmutableList.copyOf(lines.stream().map(CharUtil::stripNonPrintable).collect(Collectors.toList()));
    }
}
