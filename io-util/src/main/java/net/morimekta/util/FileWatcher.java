/*
 * Copyright (c) 2017, Stein Eldar Johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
        this.watchers = new ArrayList<>();
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
     * Start watching a specific file. Note that this will watch the
     * file as seen in the directory as it is pointed to. This means that
     * if the file itself is a symlink, then change events will notify
     * changes to the symlink definition, not the content of the file.
     * <p>
     * So if this is the case:
     * <pre>{@code
     * /home/morimekta/link -> ../morimekta/test
     * /home/morimekta/somefile.txt -> ../morimekta/test/somefile.txt
     * /home/morimekta/test/somefile.txt
     * }</pre>
     * Then calling <code>watcher.startWatching(new File("/home/morimekta/link/somefile.txt"))</code>
     * will watch changes to the file content of <code>/home/morimekta/test/somefile.txt</code>,
     * while <code>watcher.startWatching(new File("/home/morimekta/somefile.txt"))</code>
     * will only note if the symlink <code>/home/morimekta/somefile.txt</code> starts
     * pointing elsewhere.
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

            // Resolve directory to canonical directory.
            File parent = file.getAbsoluteFile()
                              .getParentFile()
                              .getCanonicalFile()
                              .getAbsoluteFile();
            // But do not canonical file, as if this is a symlink, we want to listen
            // to changes to the symlink, not the file it points to. If that is wanted
            // the file should be made canonical (resolve symlinks) before calling
            // startWatching(file).
            file = new File(parent, file.getName());

            if (watchedFiles.contains(file.toString())) {
                // We're already watching this file, do nothing.
                return;
            }

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

            // Resolve directory to canonical directory.
            File parent = file.getAbsoluteFile()
                              .getParentFile()
                              .getCanonicalFile()
                              .getAbsoluteFile();
            // But do not canonical file, as if this is a symlink, we want to listen
            // to changes to the symlink, not the file it points to. If that is wanted
            // the file should be made canonical (resolve symlinks) before calling
            // startWatching(file).
            file = new File(parent, file.getName());

            watchedFiles.remove(file.toString());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
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
                    List<Supplier<Watcher>> tmp;
                    synchronized (watchers) {
                        tmp = new ArrayList<>(watchers);
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

    private final ArrayList<Supplier<Watcher>> watchers;
    private final Map<String, WatchKey> watchDirKeys;
    private final Map<WatchKey, String> watchKeyDirs;
    private final Set<String>           watchedFiles;
    private final ExecutorService       callbackExecutor;
    private final ExecutorService       watcherExecutor;
    private final WatchService          watchService;
}
