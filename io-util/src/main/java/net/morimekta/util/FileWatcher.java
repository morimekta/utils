package net.morimekta.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import java.util.function.Supplier;

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
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

    /**
     * Add a file watcher that is persistent. If the reference from the
     * file watcher to the watcher itself should not prevent garbage collection,
     * use the {@link #weakAddWatcher(Watcher)} method.
     *
     * @param watcher The watcher to add.
     */
    public void addWatcher(Watcher watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher added");
        }

        synchronized (watchers) {
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Adding watcher on closed FileWatcher");
            }
            watchers.add(() -> watcher);
        }
    }

    /**
     * Add a non-persistent file watcher.
     *
     * @param watcher The watcher to add.
     */
    public void weakAddWatcher(Watcher watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher added");
        }

        synchronized (watchers) {
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Adding watcher on closed FileWatcher");
            }
            watchers.add(new WeakReference<>(watcher)::get);
        }
    }

    /**
     * Remove a watcher from the list of listeners.
     *
     * @param watcher The watcher to be removed.
     * @return True if the watcher was removed from the list.
     */
    public boolean removeWatcher(Watcher watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher removed");
        }

        boolean removed = false;
        synchronized (watchers) {
            Iterator<Supplier<Watcher>> iterator = watchers.iterator();
            while (iterator.hasNext()) {
                Watcher next = iterator.next().get();
                if (next == watcher) {
                    iterator.remove();
                    removed = true;
                } else if (next == null) {
                    iterator.remove();
                }
            }
        }
        return removed;
    }

    /**
     * Start watching a specific file.
     *
     * @param file The file to be watched.
     */
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
            if (!parent.exists()) {
                throw new IllegalArgumentException("Parent dir of file does not exists: " + parent.toString());
            }

            if (!watchDirKeys.containsKey(parent.toString())) {

                Path dirPath = Paths.get(parent.getAbsolutePath());
                WatchKey key = dirPath.register(watchService, new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE}, HIGH);
                watchDirKeys.put(parent.toString(), key);
                watchKeyDirs.put(key, parent.toString());
            }

            watchedFiles.add(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Stop watching a specific file.
     *
     * @param file The file to be watched.
     */
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
            if (watcherExecutor.isShutdown()) {
                return;
            }

            watchers.clear();
        }
        watcherExecutor.shutdown();
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
        callbackExecutor.shutdown();
        try {
            if (!callbackExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                LOGGER.warn("CallbackExecutor failed to terminate in 10 seconds");
            }
        } catch (InterruptedException e) {
            LOGGER.error("CallbackExecutor termination interrupted", e);
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
                    List<Supplier<Watcher>> tmp = new LinkedList<>();
                    synchronized (watchers) {
                        tmp.addAll(watchers);
                    }
                    callbackExecutor.submit(() -> {
                        for (final Supplier<Watcher> supplier : tmp) {
                            Watcher watcher = supplier.get();
                            if (watcher != null) {
                                for (final File file : updates) {
                                    try {
                                        watcher.onFileUpdate(file);
                                    } catch (RuntimeException e) {
                                        LOGGER.error("Exception when notifying update on " + file, e);
                                    }
                                }
                            } else {
                                watchers.remove(supplier);
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

    private final LinkedList<Supplier<Watcher>> watchers;
    private final Map<String, WatchKey> watchDirKeys;
    private final Map<WatchKey, String> watchKeyDirs;
    private final Set<String>           watchedFiles;
    private final ExecutorService       callbackExecutor;
    private final ExecutorService       watcherExecutor;
    private final WatchService          watchService;
}
