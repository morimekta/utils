package net.morimekta.console.terminal;

import java.util.function.LongConsumer;

/**
 * Show progress on a single task in how many percent (with spinner and
 * progress-bar). If closed before finished (updated with the total
 * progress value), it is considered 'cancelled'.
 */
public interface ProgressTask extends LongConsumer, AutoCloseable {
    @Override
    void close();

    /**
     * If the progress task is done or aborted.
     *
     * @return True if done.
     */
    boolean isDone();
}
