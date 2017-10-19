package net.morimekta.console.terminal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Color;
import net.morimekta.util.Strings;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Comparator.comparing;
import static net.morimekta.console.chr.Control.cursorUp;
import static net.morimekta.console.terminal.Progress.format;

/**
 * Show progress on a number of tasks. The tasks can be dynamically created
 * and finished. E.g if a number of large files needs to be downloaded they
 * can be given a task each, and only a certain number of files will be downloaded
 * at the same time. Example:
 *
 * <pre>{@code
 * try (ProgressManager progress = new ProgressManager(term, Progress.Spinner.CLOCK)) {
 *     Future<String> first = progress.addTask("First Task", 10000, task -> {
 *         // All the work
 *         task.accept(10000);
 *         return "OK";
 *     });
 *     Future<String> second = progress.addTask("Second Task", 10000, task -> {
 *         // All the work
 *         task.accept(10000);
 *         return "OK";
 *     });
 *
 *     progress.waitAbortable();
 *
 *     term.println("First: " + first.get());
 *     term.println("Second: " + second.get());
 * } finally {
 *     term.println();
 * }
 * }</pre>
 */
public class ProgressManager implements AutoCloseable {
    @FunctionalInterface
    public interface ProgressHandler<T> {
        T handle(@Nonnull ProgressTask progress) throws Exception;
    }

    @FunctionalInterface
    public interface ProgressAsyncHandler<T> {
        void handle(@Nonnull CompletableFuture<T> result, @Nonnull ProgressTask progress);
    }


    /**
     * Create a progress bar using the given terminal.
     *
     * @param terminal The terminal to use.
     * @param spinner  The spinner to use.
     */
    public ProgressManager(@Nonnull Terminal terminal,
                           @Nonnull Progress.Spinner spinner) {
        this(terminal,
             spinner,
             DEFAULT_MAX_TASKS);
    }

    /**
     * Create a progress bar using the given terminal.
     *
     * @param terminal The terminal to use.
     * @param spinner  The spinner to use.
     * @param max_tasks Maximum number fo concurrent inProgress.
     */
    public ProgressManager(@Nonnull Terminal terminal,
                           @Nonnull Progress.Spinner spinner,
                           int max_tasks) {
        this(terminal,
             spinner,
             max_tasks,
             Executors.newScheduledThreadPool(max_tasks + 1,
                                              new ThreadFactoryBuilder()
                                                      .setNameFormat("progress-%d")
                                                      .build()),
             Clock.systemUTC());
    }

    /**
     * Create a progress updater. Note that <b>either</b> terminal or the
     * updater param must be set.
     *
     * @param terminal The terminal to print to.
     * @param spinner  The spinner type.
     * @param executor The executor to run updater task in.
     * @param clock    The clock to use for timing.
     */
    @VisibleForTesting
    ProgressManager(Terminal terminal,
                    Progress.Spinner spinner,
                    int maxTasks,
                    ScheduledExecutorService executor,
                    Clock clock) {
        this.terminal = terminal;
        this.executor = executor;
        this.clock = clock;
        this.spinner = spinner;

        this.maxTasks = maxTasks;
        this.startedTasks = new ArrayList<>();
        this.queuedTasks = new ConcurrentLinkedQueue<>();
        this.buffer = new LineBuffer(terminal);
        this.isWaiting = new AtomicBoolean(false);
        this.updater = executor.scheduleAtFixedRate(this::doUpdate, 10L, 100L, TimeUnit.MILLISECONDS);
    }

    /**
     * Close the progress and all tasks associated with it.
     */
    @Override
    public void close() {
        synchronized (startedTasks) {
            if (executor.isShutdown()) {
                return;
            }
            // ... stop updater thread. Do not interrupt.
            executor.shutdown();
            try {
                updater.get();
            } catch (InterruptedException | CancellationException | ExecutionException ignore) {
                // Ignore these closing thread errors.
            }
            // And stop all the tasks, do interrupting.
            for (InternalTask task : startedTasks) {
                task.cancel(true);
            }
            for (InternalTask task : queuedTasks) {
                task.close();
            }

            try {
                executor.awaitTermination(100L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Wait for all scheduled tasks to finish allowing the user to abort all
     * tasks with &lt;ctrl&gt;-C.
     *
     * @throws IOException If interrupted by user.
     * @throws InterruptedException If interrupted by system or other threads.
     */
    public void waitAbortable() throws IOException, InterruptedException {
        try {
            isWaiting.set(true);
            terminal.waitAbortable(updater);
        } finally {
            close();
            updateLines();
            terminal.finish();
        }
    }

    /**
     * Add a task to be done while showing progress. If there are too many tasks
     * ongoing, the task will be queued and done when the local thread pool has
     * available threads.
     *
     * @param title The progress title of the task.
     * @param total The total progress to complete.
     * @param handler The handler to do the task behind the progress being shown.
     * @param <T> The return type for the task.
     * @return The future returning the task result.
     */
    public <T> Future<T> addTask(String title,
                                 long total,
                                 ProgressAsyncHandler<T> handler) {
        if (executor.isShutdown()) {
            throw new IllegalStateException("Adding task to closed progress manager");
        }

        InternalTask<T> task = new InternalTask<>(title, total, handler);
        queuedTasks.add(task);
        startTasks();
        return task;
    }

    /**
     * Add a task to be done while showing progress. If there are too many tasks
     * ongoing, the task will be queued and done when the local thread pool has
     * available threads.
     *
     * @param title The progress title of the task.
     * @param total The total progress to complete.
     * @param handler The handler to do the task behind the progress being shown.
     * @param <T> The return type for the task.
     * @return The future returning the task result.
     */
    public <T> Future<T> addTask(String title,
                                 long total,
                                 ProgressHandler<T> handler) {
        ProgressAsyncHandler<T> async = (result, progress) -> {
            try {
                result.complete(handler.handle(progress));
            } catch (Exception e) {
                if (!result.isCancelled()) {
                    result.completeExceptionally(e);
                }
            }
        };
        return addTask(title, total, async);
    }

    protected List<String> lines() {
        return buffer.lines();
    }

    // ------ private ------

    private static final int DEFAULT_MAX_TASKS = 5;

    private final Terminal                 terminal;
    private final ExecutorService          executor;
    private final Progress.Spinner         spinner;
    private final Clock                    clock;
    private final Future<?>                updater;
    private final ArrayList<InternalTask> startedTasks;
    private final Queue<InternalTask>      queuedTasks;
    private final LineBuffer               buffer;
    private final AtomicBoolean            isWaiting;
    private final int                      maxTasks;

    private void startTasks() {
        synchronized (startedTasks) {
            if (executor.isShutdown()) {
                return;
            }

            int toAdd = maxTasks;
            for (InternalTask task : startedTasks) {
                if (!task.isDone()) {
                    --toAdd;
                }
            }

            while (toAdd-- > 0 && !queuedTasks.isEmpty()) {
                InternalTask task = queuedTasks.poll();
                startedTasks.add(task);
                task.start();
            }
        }
    }

    private int getTerminalWidth() {
        return terminal.getTTY().getTerminalSize().cols;
    }

    private boolean isDone() {
        synchronized (startedTasks) {
            if (!queuedTasks.isEmpty()) return false;
            for (InternalTask task : startedTasks) {
                if (!task.isDone()) {
                    return false;
                }
            }
        }

        return true;
    }

    private void doUpdate() {
        updateLines();
        if (isWaiting.get() && isDone()) {
            updater.cancel(false);
        }
    }

    private void updateLines() {
        List<String> updatedLines = new ArrayList<>();
        synchronized (startedTasks) {
            // Make sure we read terminal size before each actual update.
            terminal.getTTY().clearCachedTerminalSize();

            int maxUpdating = Math.min(terminal.getTTY().getTerminalSize().rows, maxTasks * 2);

            if (startedTasks.size() > maxUpdating) {
                // If we have more started then number of available rows:

                // Clear the buffer (should clear all lines and move
                buffer.clear();
                terminal.print("" + Char.CR);

                // First try to move all completed tasks off the started task list
                // without changing the already completed order. Move the already
                // completed tasks off that are all before non-completed tasks.
                Iterator<InternalTask> it = startedTasks.iterator();
                while (it.hasNext()) {
                    InternalTask next = it.next();
                    if (next.fraction >= 1.0) {
                        terminal.print(renderTask(next));
                        terminal.println();
                        it.remove();
                    } else {
                        break;
                    }
                }

                if (startedTasks.size() > maxUpdating) {
                    // If the list of started tasks is still too large move all completed
                    // tasks first, and then repeat.

                    // SORT completed BEFORE not-completed.
                    startedTasks.sort(comparing(t -> t.fraction < 1.0));

                    it = startedTasks.iterator();
                    while (it.hasNext()) {
                        InternalTask next = it.next();
                        if (next.fraction >= 1.0) {
                            terminal.print(renderTask(next));
                            terminal.println();
                            it.remove();
                        } else {
                            break;
                        }
                    }
                }

                // And 1 back.
                terminal.print(cursorUp(1));
            }

            for (InternalTask task : startedTasks) {
                updatedLines.add(renderTask(task));
            }
            if (queuedTasks.size() > 0) {
                updatedLines.add(" -- And " + queuedTasks.size() + " more...");
            }
        }
        if (updatedLines.size() > 0) {
            while (buffer.count() < updatedLines.size()) {
                buffer.add("");
            }
            buffer.update(0, updatedLines);
        }
    }

    private String renderTask(@Nonnull InternalTask<?> task) {
        long  now   = clock.millis();

        synchronized (task) {
            int pts_w = getTerminalWidth() - 23 - task.title.length();
            int pct   = (int) (task.fraction * 100);
            int pts   = (int) (task.fraction * pts_w);

            int remaining_pts = pts_w - pts;

            if (task.isCancelled()) {
                return String.format("%s: [%s%s%s%s%s] %3d%% %sCancelled%s",
                                     task.title,

                                     Color.GREEN,
                                     Strings.times(spinner.done.toString(), pts),
                                     Color.YELLOW,
                                     Strings.times(spinner.remain.toString(), remaining_pts),
                                     Color.CLEAR,

                                     pct,

                                     new Color(Color.RED, Color.BOLD),
                                     Color.CLEAR);
            } else if (task.isCompletedExceptionally()) {
                return String.format("%s: [%s%s%s%s%s] %3d%% %sFailed%s",
                                     task.title,

                                     Color.GREEN,
                                     Strings.times(spinner.done.toString(), pts),
                                     Color.YELLOW,
                                     Strings.times(spinner.remain.toString(), remaining_pts),
                                     Color.CLEAR,

                                     pct,

                                     new Color(Color.RED, Color.BOLD),
                                     Color.CLEAR);
            } else if (task.fraction < 1.0) {
                Duration remaining = null;
                // Progress has actually gone forward, recalculate total time.
                if (task.expected_done_ts > 0) {
                    long remaining_ms = max(0L, task.expected_done_ts - now);
                    remaining = Duration.of(remaining_ms, ChronoUnit.MILLIS);
                }

                int spinner_pos = task.spinner_pos % spinner.spinner.length;

                return String.format("%s: [%s%s%s%s%s] %3d%% %s%s%s%s",
                                     task.title,

                                     Color.GREEN,
                                     Strings.times(spinner.done.toString(), pts),
                                     Color.YELLOW,
                                     Strings.times(spinner.remain.toString(), remaining_pts),
                                     Color.CLEAR,

                                     pct,

                                     new Color(Color.YELLOW, Color.BOLD),
                                     spinner.spinner[spinner_pos],
                                     Color.CLEAR,
                                     remaining == null ? "" : " + " + format(remaining));
            } else {
                return String.format("%s: [%s%s%s] 100%% %s%s%s @ %s",
                                     task.title,

                                     Color.GREEN,
                                     Strings.times(spinner.done.toString(), pts_w),
                                     Color.CLEAR,

                                     new Color(Color.GREEN, Color.BOLD),
                                     spinner.complete,
                                     Color.CLEAR,
                                     format(Duration.of(task.updated_ts - task.started_ts, ChronoUnit.MILLIS)));
            }
        }
    }

    class InternalTask<T> extends CompletableFuture<T> implements Runnable, ProgressTask {
        final long                       total;
        final long                       created_ts;
        final String                     title;
        final AtomicReference<Future<?>> future;
        final ProgressAsyncHandler<T>    handler;

        volatile int     spinner_pos;
        volatile long    spinner_update_ts;
        volatile long    started_ts;
        volatile long    updated_ts;
        volatile long    updated_time_ts;
        volatile long    expected_done_ts;
        volatile double  fraction;

        /**
         * Create a progress updater. Note that <b>either</b> terminal or the
         * updater param must be set.
         *
         * @param title What progresses.
         * @param total The total value to be 'progressed'.
         */
        InternalTask(String title,
                     long total,
                     ProgressAsyncHandler<T> handler) {
            this.title = title;
            this.total = total;
            this.handler = handler;

            this.future = new AtomicReference<>();

            this.spinner_pos = 0;
            this.spinner_update_ts = 0L;
            this.created_ts = clock.millis();
            this.started_ts = 0;
            this.fraction = 0.0;
        }

        void start() {
            synchronized (this) {
                if (isCancelled()) {
                    throw new IllegalStateException("Starting cancelled task");
                }
                if (started_ts > 0) {
                    throw new IllegalStateException("Already Started");
                }
                started_ts = clock.millis();
                spinner_update_ts = started_ts;

                future.set(executor.submit(() -> {
                        try {
                            handler.handle(this, this);
                        } catch (Exception e) {
                            if (!isCancelled()) {
                                completeExceptionally(e);
                            }
                        }
                }));
            }
        }

        @Override
        public void run() {
            synchronized (this) {
                if (isDone()) {
                    return;
                }
                if (started_ts > 0) {
                    throw new IllegalStateException("Already Started");
                }
                started_ts = clock.millis();
                spinner_update_ts = started_ts;
            }
            try {
                handler.handle(this, this);
            } catch (Exception e) {
                completeExceptionally(e);
            }
        }

        @Override
        public boolean complete(T t) {
            try {
                synchronized (this) {
                    if (isDone()) {
                        return false;
                    }
                    accept(total);
                    return super.complete(t);
                }
            } finally {
                startTasks();
            }
        }

        @Override
        public boolean completeExceptionally(Throwable throwable) {
            try {
                synchronized (this) {
                    if (isDone()) {
                        return false;
                    }
                    stopInternal();
                    return super.completeExceptionally(throwable);
                }
            } finally {
                startTasks();
            }
        }

        @Override
        public boolean cancel(boolean interruptable) {
            try {
                synchronized (this) {
                    if (isDone()) {
                        return false;
                    }
                    stopInternal();
                    super.cancel(interruptable);
                }

                Future<?> f = future.get();
                return f != null && !f.isDone() && f.cancel(interruptable);
            } finally {
                startTasks();
            }
        }

        @Override
        public void close() {
            cancel(true);
        }

        /**
         * Update the progress to reflect the current progress value.
         *
         * @param current The new current progress value.
         */
        @Override
        public void accept(long current) {
            synchronized (this) {
                if (isCancelled()) {
                    throw new IllegalStateException("Task is cancelled");
                }
                if (isDone()) return;

                long now = clock.millis();
                current = min(current, total);

                if (now >= (spinner_update_ts + 100)) {
                    spinner_pos = spinner_pos + 1;
                    spinner_update_ts = now;
                }

                if (current < total) {
                    fraction = ((double) current) / ((double) total);

                    long duration_ms = now - started_ts;
                    if (duration_ms > 3000) {
                        // Progress has actually gone forward, recalculate total time only if
                        // we have 3 second of progress.
                        if (expected_done_ts == 0L || updated_time_ts < (now - 2000L)) {
                            // Update total / expected time once per 2 seconds.
                            long assumed_total = (long) (((double) duration_ms) / fraction);
                            long remaining_ms  = max(0L, assumed_total - duration_ms);
                            expected_done_ts = now + remaining_ms;
                            updated_time_ts = now;
                        }
                    }
                } else {
                    fraction = 1.0;
                    expected_done_ts = now;
                }
                updated_ts = now;
            }
        }

        private void stopInternal() {
            long now = clock.millis();
            this.updated_ts = now;
            this.updated_time_ts = now;
            this.spinner_update_ts = now;
            this.expected_done_ts = 0L;
        }
    }
}
