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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.sun.nio.file.SensitivityWatchEventModifier.HIGH;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import static net.morimekta.util.FileUtil.readCanonicalPath;

/**
 * File watcher helper for use with simple callbacks. It monitors two
 * types of changes: Actual file changes, and symlink changes. Note that
 * whenever a file or symlink is added to the whole, it will be monitored
 * at least until the file or symlink is deleted.
 *
 * There are essentially two types of watchers. File specific watchers will
 * always resolve back to the "requested" file, and it is triggered whenever
 * any of:
 * <ul>
 *     <li>The current canonical file content changed.</li>
 *     <li>The link starts pointing to a different file, this
 *         is only calculated based on the file being a symlink,
 *         or being in a symlinked directory. Everything else
 *         is treated as canonical locations.</li>
 * </ul>
 * <p>
 * So if this is the case (ref configMaps in kubernetes):
 * <pre>{@code
 * /volume/map/..2018-01/config.txt (old file)
 * /volume/map/..2018-02/config.txt (new file)
 * /volume/map/..data     -> symlink to '..2018-01'
 * /volume/map/config.txt -> symlink to '..data/config.txt'
 * }</pre>
 *
 * If you listen to '/volume/map/config.txt', then you are notified if:
 * <ul>
 *     <li>'/volume/map/..2018-01/config.txt' is changed</li>
 *     <li>'/volume/map/..data' symlink is updated to '..2018-02'</li>
 *     <li>'/volume/map/config.txt' symlink is updated to '..2018-02/config.txt'</li>
 * </ul>
 *
 * The important case here is the middle one, as this is the way kubernetes
 * handles its configMaps and Secrets.
 */
public class FileWatcher implements AutoCloseable {
    /**
     * @deprecated Use {@link Listener} instead.
     */
    @Deprecated
    @FunctionalInterface
    public interface Watcher extends Listener {
        void onFileUpdate(File file);
        @Override
        default void onPathUpdate(Path path) {
            onFileUpdate(path.toFile());
        }
    }

    @FunctionalInterface
    public interface Listener {
        /**
         * Called when the requested file path is updated.
         *
         * @param path Path to the file updated.
         */
        void onPathUpdate(Path path);
    }

    /**
     * Create a FileWatcher with default watch service.
     */
    public FileWatcher() {
        this(newWatchService());
    }

    /**
     * Create a FileWatcher using the provided watch service.
     * @param watchService Watcher service to use.
     */
    public FileWatcher(WatchService watchService) {
        this(watchService,
             Executors.newSingleThreadExecutor(makeThreadFactory("FileWatcher")),
             Executors.newSingleThreadExecutor(makeThreadFactory("FileWatcherCallback")));
    }

    // @VisibleForTesting
    protected FileWatcher(WatchService watchService,
                          ExecutorService watcherExecutor,
                          ExecutorService callbackExecutor) {
        this.mutex = new Object();

        this.watchers = new ArrayList<>();

        this.watchDirKeys = new HashMap<>();
        this.watchKeyDirs = new HashMap<>();
        this.watchedFiles = Collections.synchronizedMap(new HashMap<>());
        this.requestToTarget = new HashMap<>();
        this.targetToRequests = new HashMap<>();
        this.watchService = watchService;
        this.watcherExecutor = watcherExecutor;
        this.callbackExecutor = callbackExecutor;
        this.watcherExecutor.submit(this::watchFilesTask);
    }

    /**
     * Start watching file path and notify watcher for updates on that file.
     *
     * @param file The file path to watch.
     * @param watcher The watcher to be notified.
     * @deprecated Use {@link #addWatcher(Path, Listener)}
     */
    @Deprecated
    public void addWatcher(File file, Listener watcher) {
        addWatcher(file.toPath(), watcher);
    }

    /**
     * Start watching file path and notify watcher for updates on that file.
     *
     * @param file The file path to watch.
     * @param watcher The watcher to be notified.
     */
    public void addWatcher(Path file, Listener watcher) {
        if (file == null) throw new IllegalArgumentException("Null file argument");
        if (watcher == null) throw new IllegalArgumentException("Null watcher argument");
        synchronized (mutex) {
            startWatchingInternal(file).add(() -> watcher);
        }
    }

    /**
     * Add a file watcher that is persistent. If the reference from the
     * file watcher to the watcher itself should not prevent garbage collection,
     * use the {@link #weakAddWatcher(Listener)} method.
     *
     * @param watcher The watcher to add.
     * @deprecated Use {@link #addWatcher(File, Listener)}
     */
    @Deprecated
    public void addWatcher(Listener watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher added");
        }

        synchronized (mutex) {
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Adding watcher on closed FileWatcher");
            }
            watchers.add(() -> watcher);
        }
    }

    /**
     * Start watching file path and notify watcher for updates on that file.
     * The watcher will be kept in a weak reference and will allow GC to delete
     * the instance.
     *
     * @param file The file path to watch.
     * @param watcher The watcher to be notified.
     * @deprecated Use {@link #weakAddWatcher(Path, Listener)}
     */
    @Deprecated
    public void weakAddWatcher(File file, Listener watcher) {
        weakAddWatcher(file.toPath(), watcher);
    }

    /**
     * Start watching file path and notify watcher for updates on that file.
     * The watcher will be kept in a weak reference and will allow GC to delete
     * the instance.
     *
     * @param file The file path to watch.
     * @param watcher The watcher to be notified.
     */
    public void weakAddWatcher(Path file, Listener watcher) {
        synchronized (mutex) {
            startWatchingInternal(file).add(new WeakReference<>(watcher)::get);
        }
    }

    /**
     * Add a non-persistent file watcher.
     *
     * @param watcher The watcher to add.
     * @deprecated Use {@link #weakAddWatcher(File, Listener)}.
     */
    @Deprecated
    public void weakAddWatcher(Listener watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher added");
        }

        synchronized (mutex) {
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
    public boolean removeWatcher(Listener watcher) {
        if (watcher == null) {
            throw new IllegalArgumentException("Null watcher removed");
        }

        synchronized (mutex) {
            AtomicBoolean removed = new AtomicBoolean(removeFromListeners(watchers, watcher));
            watchedFiles.forEach((path, suppliers) -> {
                if (removeFromListeners(suppliers, watcher)) {
                    removed.set(true);
                }
            });
            return removed.get();
        }
    }

    /**
     * Start watching a specific file. Note that this will watch the
     * file as seen in the directory as it is pointed to. This means that
     * if the file itself is a symlink, then change events will notify
     * changes to the symlink definition, not the content of the file.
     *
     * @param file The file to be watched.
     * @deprecated Use {@link #addWatcher(File, Listener)} or {@link #weakAddWatcher(File, Listener)}
     */
    @Deprecated
    public void startWatching(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Null file argument");
        }
        synchronized (mutex) {
            startWatchingInternal(file.toPath());
        }
    }

    /**
     * Stop watching a specific file.
     *
     * @param file The file to be watched.
     * @deprecated Use {@link #removeWatcher(Listener)} and let it clean up watched
     *             files itself.
     */
    @Deprecated
    public void stopWatching(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Null file argument");
        }
        synchronized (mutex) {
            stopWatchingInternal(file.toPath());
        }
    }

    @Override
    public void close() {
        synchronized (mutex) {
            if (watcherExecutor.isShutdown()) {
                return;
            }

            watchers.clear();
            targetToRequests.clear();
            requestToTarget.clear();
            watchedFiles.clear();
            watchDirKeys.clear();
            watchKeyDirs.clear();
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

    private <T> boolean removeFromListeners(@Nonnull List<Supplier<T>> listeners,
                                            @Nullable T listener) {
        Iterator<Supplier<T>> iterator = listeners.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            T next = iterator.next().get();
            if (next == listener) {
                iterator.remove();
                removed = true;
            } else if (next == null) {
                iterator.remove();
            }
        }
        return removed;
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

    private List<Supplier<Listener>> startWatchingInternal(@Nonnull Path path) {
        try {
            if (watcherExecutor.isShutdown()) {
                throw new IllegalStateException("Starts to watch on closed FileWatcher");
            }

            path = path.toAbsolutePath();
            // Resolve directory to canonical directory.
            Path parent = readCanonicalPath(path.getParent());
            // But do not canonical file, as if this is a symlink, we want to listen
            // to changes to the symlink, not just the file it points to. If that is wanted
            // the file should be made canonical (resolve symlinks) before calling
            // startWatching(file).
            path = parent.resolve(path.getFileName());

            for (Path add : linkTargets(path)) {
                if (Files.isSymbolicLink(add.getParent())) {
                    addDirectoryWatcher(add.getParent().getParent());
                }
                addDirectoryWatcher(add.getParent());
            }
            addLinkTargetting(path);

            return watchedFiles.computeIfAbsent(path, fp -> new ArrayList<>());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void addDirectoryWatcher(Path directory) {
        watchDirKeys.computeIfAbsent(directory, dir -> {
            try {
                WatchKey key = directory.register(
                        watchService,
                        new WatchEvent.Kind[]{ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE},
                        HIGH);
                watchKeyDirs.put(key, directory);
                return key;
            } catch (IOException e) {
                throw new UncheckedIOException(e.getMessage(), e);
            }
        });
    }

    private void stopWatchingInternal(@Nonnull Path path) {
        try {
            path = path.toAbsolutePath();
            // Resolve directory to canonical directory.
            Path parent = readCanonicalPath(path.getParent());
            // But do not canonical file, as if this is a symlink, we want to listen
            // to changes to the symlink, not the file it points to. If that is wanted
            // the file should be made canonical (resolve symlinks) before calling
            // startWatching(file).
            path = parent.resolve(path.getFileName());

            removeLinkTargetting(path);
            watchedFiles.remove(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Handle the watch service event loop.
     */
    private void watchFilesTask() {
        while (!watcherExecutor.isShutdown()) {
            try {
                WatchKey key = watchService.take();
                Path parent = watchKeyDirs.get(key);
                if (parent == null) {
                    key.reset();
                    continue;
                }

                Set<Path> updates = new TreeSet<>();
                for (WatchEvent<?> ev : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = ev.kind();

                    // Only file modification, creation, deletion is interesting.
                    if (kind != ENTRY_MODIFY &&
                        kind != ENTRY_CREATE &&
                        kind != ENTRY_DELETE) {
                        if (kind == OVERFLOW) {
                            LOGGER.warn("Overflow event, file updates may have been lost");
                        }
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> event = (WatchEvent<Path>) ev;

                    Path file = parent.resolve(event.context());
                    if (targetToRequests.containsKey(file)) {
                        LOGGER.trace("Watched file " + file + " event " + kind);
                        updates.add(file);
                    }
                }
                // Ready the key again so it wil signal more events.
                key.reset();

                if (updates.size() > 0) {
                    List<Supplier<Listener>>           watcherList;
                    Map<Path,List<Supplier<Listener>>> watcherMap;
                    Set<Path>                          updatedRequests = new TreeSet<>();
                    synchronized (mutex) {
                        watcherList = new ArrayList<>(this.watchers);
                        watcherMap = deepCopy(watchedFiles);

                        for (final Path file : updates) {
                            updatedRequests.addAll(targetToRequests.getOrDefault(file, Collections.emptySet()));
                        }
                        for (final Path file : updates) {
                            try {
                                updateLinkTargetting(file);
                            } catch (IOException e) {
                                LOGGER.warn("Failed to update link targeting: {}", e.getMessage(), e);
                            }
                        }
                    }

                    callbackExecutor.submit(() -> {
                        for (final Path file : updates) {
                            for (final Supplier<Listener> supplier : watcherList) {
                                Listener watcher = supplier.get();
                                if (watcher != null) {
                                    try {
                                        watcher.onPathUpdate(file);
                                    } catch (RuntimeException e) {
                                        LOGGER.error("Exception when notifying update on " + file, e);
                                    }
                                }
                            }
                        }
                        for (Path request : updatedRequests) {
                            Optional.ofNullable(watcherMap.get(request))
                                    .ifPresent(list -> {
                                        for (final Supplier<Listener> supplier : list) {
                                            Listener watcher = supplier.get();
                                            if (watcher != null) {
                                                try {
                                                    watcher.onPathUpdate(request);
                                                } catch (RuntimeException e) {
                                                    LOGGER.error("Exception when notifying update on " + request, e);
                                                }
                                            }
                                        }
                                    });
                        }
                    });
                }
            } catch (InterruptedException interruptedEx) {
                LOGGER.error("Interrupted in service file watch thread: " + interruptedEx.getMessage(), interruptedEx);
                return;
            }
        }
    }

    @Nonnull
    private static Map<Path,List<Supplier<Listener>>> deepCopy(@Nonnull Map<Path,List<Supplier<Listener>>> in) {
        Map<Path,List<Supplier<Listener>>> out = new HashMap<>();
        in.forEach((path, suppliers) -> out.put(path, new ArrayList<>(suppliers)));
        return in;
    }

    private static WatchService newWatchService() {
        try {
            return FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // List of targets for a file or link, ending with the
    // canonical target of the link. The first entry is always the
    // requested file / link, and the last entry the canonical file.
    private List<Path> linkTargets(Path link) throws IOException {
        link = link.toAbsolutePath();

        List<Path> out = new ArrayList<>();
        out.add(link);

        if (Files.isSymbolicLink(link.getParent())) {
            Path target = link.getParent()
                              .getParent()
                              .resolve(Files.readSymbolicLink(link.getParent()))
                              .resolve(link.getFileName())
                              .toAbsolutePath();
            out.addAll(linkTargets(target));
        } else if (Files.isSymbolicLink(link)) {
            Path target = link.getParent()
                              .resolve(Files.readSymbolicLink(link))
                              .toAbsolutePath();
            out.addAll(linkTargets(target));
        } else {
            // Then resolve the rest.
            Path canonical = readCanonicalPath(link);
            if (!canonical.equals(link)) {
                out.add(canonical);
            }
        }

        return out;
    }

    private Set<Path> getAffectedRequests(Path change) {
        Set<Path> out = new HashSet<>();
        out.add(change);
        out.addAll(targetToRequests.getOrDefault(change, new HashSet<>()));
        return out;
    }

    private void updateLinkTargetting(Path change) throws IOException {
        Set<Path> affected = getAffectedRequests(change);
        Set<Path> originalRequests = new HashSet<>(requestToTarget.keySet());
        originalRequests.retainAll(affected);
        for (Path request : originalRequests) {
            removeLinkTargetting(request);
            if (Files.exists(request)) {
                addLinkTargetting(request);
            }
        }
    }

    private void removeLinkTargetting(Path request) {
        requestToTarget.remove(request);

        Set<Path> removeTargets = new HashSet<>();
        for (Map.Entry<Path, Set<Path>> entry : targetToRequests.entrySet()) {
            entry.getValue().remove(request);
            if (entry.getValue().isEmpty()) {
                removeTargets.add(entry.getKey());
            }
        }
        for (Path target : removeTargets) {
            targetToRequests.remove(target);
        }
    }

    private void addLinkTargetting(Path request) throws IOException {
        List<Path> links = linkTargets(request);
        Path canonical = links.get(links.size() - 1);
        for (Path path : links) {
            requestToTarget.put(path, canonical);
            targetToRequests.computeIfAbsent(path, l -> new HashSet<>()).add(request);
            if (Files.isSymbolicLink(path.getParent())) {
                targetToRequests.computeIfAbsent(path.getParent(), l -> new HashSet<>()).add(request);
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcher.class);

    private final Object                              mutex;
    // List of listeners.
    private final ArrayList<Supplier<Listener>>       watchers;
    // Watched files, as a map from requested file path to list of watchers.
    private final Map<Path, List<Supplier<Listener>>> watchedFiles;

    // Directory to watcher KEY.
    private final Map<Path, WatchKey> watchDirKeys;
    // Watcher KEY to Directory.
    private final Map<WatchKey, Path> watchKeyDirs;
    // Path -> Canonical Path
    private final Map<Path,Path>      requestToTarget;
    // Path -> Link to that path.
    private final Map<Path,Set<Path>> targetToRequests;

    private final ExecutorService                    callbackExecutor;
    private final ExecutorService                    watcherExecutor;
    private final WatchService                       watchService;
}
