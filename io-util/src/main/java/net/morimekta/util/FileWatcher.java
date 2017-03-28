package net.morimekta.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * File watcher helper for use with simple callbacks.
 */
public class FileWatcher implements AutoCloseable {
    @FunctionalInterface
    public interface Watcher {
        void onFileUpdate(File file);
    }

    public FileWatcher() {
        this(newWatchService(),
             Executors.newSingleThreadExecutor(makeThreadFactory("FileWatcher")),
             Executors.newSingleThreadExecutor(makeThreadFactory("FileWatcherCallback")));
    }

    // @VisibleForTesting
    protected FileWatcher(WatchService watchService,
                          ExecutorService watcherExecutor,
                          ExecutorService callbackExecutor) {
        this.watchers = new LinkedList<>();
        this.watchDirKeys = new HashMap<>();
        this.watchKeyDirs = new HashMap<>();
        this.watchedFiles = Collections.synchronizedSet(new HashSet<>());
        this.watchService = watchService;
        this.watcherExecutor = watcherExecutor;
        this.callbackExecutor = callbackExecutor;
        this.watcherExecutor.submit(this::watchFilesTask);
    }

    public void addWatcher(Watcher watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher added");
        }

        synchronized (watchers) {
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Adding watcher on closed FileWatcher");
            }
            watchers.add(watcher);
        }
    }

    public boolean removeWatcher(Watcher watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher removed");
        }

        synchronized (watchers) {
            return watchers.remove(watcher);
        }
    }

    public void startWatching(File file) {
        try {
            if (file == null) {
                throw new IllegalArgumentException("Null file argument");
            }
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Starts to watch on closed FileWatcher");
            }

            file = file.getCanonicalFile()
                       .getAbsoluteFile();

            if (watchedFiles.contains(file.toString())) {
                // We're already watching this file, do nothing.
                return;
            }

            File parent = file.getParentFile();
            if (!watchDirKeys.containsKey(parent.toString())) {

                Path dirPath = Paths.get(parent.getAbsolutePath());
                WatchKey key = dirPath.register(watchService, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE}, HIGH);
                watchDirKeys.put(parent.toString(), key);
                watchKeyDirs.put(key, parent.toString());
            }

            watchedFiles.add(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void stopWatching(File file) {
        try {
            if (file == null) {
                throw new IllegalArgumentException("Null file argument");
            }
            file = file.getCanonicalFile()
                       .getAbsoluteFile();

            watchedFiles.remove(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (watchers) {
            watchers.clear();
        }
        watcherExecutor.shutdownNow();
        try {
            watchService.close();
        } catch (IOException e) {
            LOGGER.error("WatchService did not close", e);
        }
        try {
            if (!watcherExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warn("WatcherExecutor failed to terminate in 10 seconds");
            }
        } catch (InterruptedException e) {
            LOGGER.error("WatcherExecutor termination interrupted", e);
        }
    }

    private static ThreadFactory makeThreadFactory(String name) {
        AtomicInteger idx = new AtomicInteger();
        return runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName(name + "-" + idx.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    /**
     * Handle the watch service event loop.
     */
    private void watchFilesTask() {
        while (!watcherExecutor.isShutdown()) {
            try {
                WatchKey key = watchService.take();
                String parent = watchKeyDirs.get(key);
                if (parent == null) {
                    key.reset();
                    continue;
                }

                Set<File> updates = new TreeSet<>();
                for (WatchEvent<?> ev : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = ev.kind();

                    // Only file modification is interesting.
                    if (kind != ENTRY_MODIFY && kind != ENTRY_CREATE) {
                        if (kind == OVERFLOW) {
                            LOGGER.warn("Overflow event, file updates may have been lost");
                        }
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> event = (WatchEvent<Path>) ev;

                    File file = new File(parent, event.context().toString());
                    if (watchedFiles.contains(file.toString())) {
                        LOGGER.trace("Watched file " + file + " event " + kind);
                        updates.add(file);
                    }
                }
                // Ready the key again so it vil signal more events.
                key.reset();

                if (updates.size() > 0) {
                    List<Watcher> tmp = new LinkedList<>();
                    synchronized (watchers) {
                        tmp.addAll(watchers);
                    }
                    callbackExecutor.submit(() -> {
                        for (final Watcher watcher : tmp) {
                            for (final File file : updates) {
                                try {
                                    watcher.onFileUpdate(file);
                                } catch (RuntimeException e) {
                                    LOGGER.error("Exception when notifying update on " + file, e);
                                }
                            }
                        }
                    });
                }
            } catch (InterruptedException interruptedEx) {
                LOGGER.error("Interrupted in service file watch thread: " + interruptedEx.getMessage(), interruptedEx);
                return;
            }
        }
    }

    private static WatchService newWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private final LinkedList<Watcher>   watchers;
    private final Map<String, WatchKey> watchDirKeys;
    private final Map<WatchKey, String> watchKeyDirs;
    private final Set<String>           watchedFiles;
    private final ExecutorService       callbackExecutor;
    private final ExecutorService       watcherExecutor;
    private final WatchService          watchService;
}
