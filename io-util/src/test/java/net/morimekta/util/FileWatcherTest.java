package net.morimekta.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class FileWatcherTest {
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private Appender<ILoggingEvent>       appender;
    private ArgumentCaptor<ILoggingEvent> eventCaptor;

    private FileWatcher sut;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws IOException {
        Awaitility.setDefaultPollDelay(new Duration(50, TimeUnit.MILLISECONDS));

        eventCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        appender = mock(Appender.class);
        doNothing().when(appender).doAppend(eventCaptor.capture());

        Logger logger = (Logger) LoggerFactory.getLogger(FileWatcher.class);
        logger.addAppender(appender);

        sut = new FileWatcher();
    }

    @After
    public void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(FileWatcher.class);
        logger.detachAppender(appender);

        sut.close();
    }

    @Test
    public void testSimpleWatch() throws IOException, InterruptedException {
        Path test1 = temp.newFile("test1").getCanonicalFile().toPath();
        Path test2 = temp.newFile("test2").getCanonicalFile().toPath();
        Path test3 = new File(temp.getRoot(), "test3").getCanonicalFile().toPath();
        temp.newFile("test4");
        Path test5 = new File(temp.getRoot(), "test5").getCanonicalFile().toPath();

        Path subDir = temp.newFolder("test-" + new Random().nextInt()).toPath();
        Path subFile = subDir.resolve("subFile");

        write("1", test1);
        write("2", test2);
        write("3", subFile);

        sleep(100L);

        FileWatcher.Listener watcher = mock(FileWatcher.Listener.class);

        sut.addWatcher(test1, watcher);  // written to.
        sut.addWatcher(test3, watcher);  // copied to
        sut.weakAddWatcher(test5.toFile(), watcher);  // no event.

        write("4", test1);
        write("5", test2);  // no event .
        Files.move(subFile, test3, REPLACE_EXISTING, ATOMIC_MOVE);

        sleep(100L);

        // NOTE: Since a write may be thread-interrupted it may arrive as a
        // set of updates on the other side.

        // no callback on write to test2.
        verify(watcher, atLeastOnce()).onPathUpdate(eq(test1));
        verify(watcher, atLeastOnce()).onPathUpdate(eq(test3));
        verifyNoMoreInteractions(watcher);
    }

    @Test
    public void testStopWatching() throws IOException, InterruptedException {
        Path file1 = temp.newFile().getCanonicalFile().toPath();
        Path file2 = temp.newFile().getCanonicalFile().toPath();

        sut.startWatching(file1.toFile());
        sut.startWatching(file2.toFile());

        writeAndMove("a", file1);
        writeAndMove("b", file2);

        sleep(100L);

        sut.stopWatching(file2.toFile());

        FileWatcher.Listener watcher = mock(FileWatcher.Listener.class);
        sut.addWatcher(watcher);

        writeAndMove("c", file2);
        writeAndMove("d", file1);

        sleep(100L);

        verify(watcher).onPathUpdate(file1);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    public void testWeakWatchers() throws IOException {
        Path file1 = temp.newFile().getCanonicalFile().toPath();
        writeAndMove("a", file1);

        sut.startWatching(file1.toFile());

        AtomicBoolean        watcherCalled = new AtomicBoolean();
        FileWatcher.Listener watcher       = f -> watcherCalled.set(true);

        FileWatcher.Listener innerWatcher = mock(FileWatcher.Listener.class);
        FileWatcher.Listener weakWatcher  = f -> innerWatcher.onPathUpdate(f);

        sut.addWatcher(watcher);
        sut.weakAddWatcher(weakWatcher);

        System.gc();

        writeAndMove("b", file1);

        await().atMost(Duration.ONE_MINUTE).untilTrue(watcherCalled);
        watcherCalled.set(false);

        verify(innerWatcher).onPathUpdate(file1);

        verifyNoMoreInteractions(innerWatcher);
        reset(innerWatcher);

        weakWatcher = null;
        assertThat(weakWatcher, is(nullValue()));

        System.gc();

        writeAndMove("c", file1);

        await().atMost(Duration.ONE_MINUTE).untilTrue(watcherCalled);
        verifyZeroInteractions(innerWatcher);
    }

    @Test
    public void testRemoveWeakWatchers() throws IOException, InterruptedException {
        AtomicBoolean        called = new AtomicBoolean();
        FileWatcher.Listener w0     = f -> called.set(true);
        sut.weakAddWatcher(w0);

        Path file1 = temp.newFile().getCanonicalFile().toPath();
        sut.startWatching(file1.toFile());
        writeAndMove("b", file1);

        await().atMost(Duration.ONE_MINUTE).untilTrue(called);
        called.set(false);

        w0 = null;
        assertThat(w0, is(nullValue()));

        System.gc();

        sleep(100);

        assertThat(sut.removeWatcher(f -> System.exit(0)), is(false));
    }

    /**
     * Test that we can have multiple watchers in parallel and still have all
     * of them working fine.
     *
     * @throws IOException On IO exception
     * @throws InterruptedException In interrupted thread.
     */
    @Test
    public void testMultipleWatchers() throws IOException, InterruptedException {
        FileWatcher          sut2   = new FileWatcher();
        AtomicInteger        called = new AtomicInteger(0);
        FileWatcher.Listener w0     = f -> called.incrementAndGet();
        FileWatcher.Listener w1     = mock(FileWatcher.Listener.class);
        FileWatcher.Listener w2     = mock(FileWatcher.Listener.class);
        FileWatcher.Listener w3     = mock(FileWatcher.Listener.class);
        FileWatcher.Listener w4     = mock(FileWatcher.Listener.class);

        sut.addWatcher(w0);
        sut.addWatcher(w1);
        sut.addWatcher(w2);

        sut2.addWatcher(w0);
        sut2.addWatcher(w3);
        sut2.addWatcher(w4);

        Path f1 = temp.newFile().getCanonicalFile().toPath();
        Path f2 = temp.newFile().getCanonicalFile().toPath();
        Path f12 = temp.newFile().getCanonicalFile().toPath();
        Files.deleteIfExists(f12);

        sut.startWatching(f1.toFile());
        sut2.startWatching(f2.toFile());
        sut.startWatching(f12.toFile());
        sut2.startWatching(f12.toFile());

        // The "writeAndMove" version should have an atomic "CREATE" event, so
        // there is no intermediate "partially written" state to count.
        writeAndMove("a", f1);
        writeAndMove("b", f2);
        writeAndMove("c", f12);

        await().atMost(1, MINUTES).untilAtomic(called, is(4));
        called.set(0);

        verify(w1).onPathUpdate(eq(f1));
        verify(w1).onPathUpdate(eq(f12));
        verify(w2).onPathUpdate(eq(f1));
        verify(w2).onPathUpdate(eq(f12));

        verify(w3).onPathUpdate(eq(f2));
        verify(w3).onPathUpdate(eq(f12));
        verify(w4).onPathUpdate(eq(f2));
        verify(w4).onPathUpdate(eq(f12));

        verifyNoMoreInteractions(w1, w2, w3, w4);

        // ------------- part 2 -------------
        // Removed watchers, and make sure they don't receive events.

        reset(w1, w2, w3, w4);

        assertThat(sut.removeWatcher(w2), is(true));
        assertThat(sut2.removeWatcher(w4), is(true));

        writeAndMove("d", f1);
        writeAndMove("e", f2);
        writeAndMove("f", f12);

        await().atMost(1, MINUTES).untilAtomic(called, is(4));
        called.set(0);

        verify(w1).onPathUpdate(eq(f1));
        verify(w1).onPathUpdate(eq(f12));

        verify(w3).onPathUpdate(eq(f2));
        verify(w3).onPathUpdate(eq(f12));

        verifyNoMoreInteractions(w1, w2, w3, w4);
    }

    @Test
    public void testMultipleDirectories() throws IOException, InterruptedException {
        AtomicInteger        called = new AtomicInteger(0);
        FileWatcher.Listener w0     = f -> called.incrementAndGet();

        Path dir1 = temp.newFolder("dir1").toPath();
        Path dir2 = temp.newFolder("dir2").toPath();

        Path f1 = dir1.resolve("f1");
        Path f2 = dir2.resolve("f2");

        FileWatcher.Listener watcher = mock(FileWatcher.Listener.class);
        sut.addWatcher(w0);
        sut.addWatcher(watcher);
        sut.startWatching(f1.toFile());
        sut.startWatching(new File(f1.toString()));
        sut.startWatching(f2.toFile());

        writeAndMove("1", f1);
        writeAndMove("2", f2);

        await().atMost(1, MINUTES).untilAtomic(called, is(2));

        verify(watcher).onPathUpdate(f1);
        verify(watcher).onPathUpdate(f2);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    public void testWatcherArguments() throws IOException {
        try {
            sut.addWatcher(null);
            fail("No exception on null watcher");
        } catch (IllegalArgumentException e) {
            assertEquals("Null watcher added", e.getMessage());
        }
        try {
            sut.weakAddWatcher(null);
            fail("No exception on null watcher");
        } catch (IllegalArgumentException e) {
            assertEquals("Null watcher added", e.getMessage());
        }

        try {
            sut.removeWatcher(null);
            fail("No exception on null watcher");
        } catch (IllegalArgumentException e) {
            assertEquals("Null watcher removed", e.getMessage());
        }

        sut.close();

        FileWatcher.Listener watcher = mock(FileWatcher.Listener.class);

        try {
            sut.addWatcher(watcher);
            fail("No exception on closed watcher");
        } catch (IllegalStateException e) {
            assertEquals("Adding watcher on closed FileWatcher", e.getMessage());
        }
        try {
            sut.weakAddWatcher(watcher);
            fail("No exception on closed watcher");
        } catch (IllegalStateException e) {
            assertEquals("Adding watcher on closed FileWatcher", e.getMessage());
        }
    }

    @Test
    public void testSymlinkDirectory() throws IOException {
        Path root = temp.getRoot()
                        .getCanonicalFile()
                        .getAbsoluteFile()
                        .toPath();
        Path someDir = root.resolve("test");
        Files.createDirectories(someDir);
        Path someSym = root.resolve("link");
        Path file = someDir.resolve("test.txt");
        Path toListen = someSym.resolve("test.txt");
        write("test", file);

        Files.createSymbolicLink(someSym, someDir);

        AtomicBoolean        updated    = new AtomicBoolean();
        ArgumentCaptor<Path> fileCaptor = ArgumentCaptor.forClass(Path.class);
        FileWatcher.Listener watcher    = mock(FileWatcher.Listener.class);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onPathUpdate(fileCaptor.capture());

        sut.addWatcher(watcher);
        sut.startWatching(toListen.toFile());

        writeAndMove("other", file);

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onPathUpdate(file);
        assertThat(fileCaptor.getValue(), is(file));
    }

    @Test
    public void testSymlinkDirectory_configMap() throws IOException, InterruptedException {
        Path root = temp.getRoot()
                        .getCanonicalFile()
                        .getAbsoluteFile()
                        .toPath();

        Path configMap = root.resolve("map");
        Files.createDirectories(configMap);
        Path configFile = configMap.resolve("test.file");
        Files.createSymbolicLink(configFile, Paths.get("..data/test.file"));


        Path oldData = configMap.resolve("..2018-01");
        Files.createDirectories(oldData);
        Path oldFile = oldData.resolve("test.file");
        write("old", oldFile);

        Path data = configMap.resolve("..data");
        Files.createSymbolicLink(data, Paths.get(oldData.getFileName().toString()));

        AtomicBoolean        updated    = new AtomicBoolean();
        ArgumentCaptor<Path> fileCaptor = ArgumentCaptor.forClass(Path.class);
        FileWatcher.Listener watcher    = mock(FileWatcher.Listener.class);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onPathUpdate(fileCaptor.capture());

        sut.addWatcher(configFile, watcher);

        // ------------------
        // Update symlink directory to new data dir, and see that it triggers.

        Path newData = configMap.resolve("..2018-02");
        Files.createDirectories(newData);
        Path newFile = newData.resolve("test.file");
        write("new", newFile);

        Path tmp = configMap.resolve("..tmp");
        Files.createSymbolicLink(tmp, Paths.get(newData.getFileName().toString()));
        Files.move(tmp, data, REPLACE_EXISTING, ATOMIC_MOVE);

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onPathUpdate(configFile);
        assertThat(fileCaptor.getValue(), is(configFile));
        verifyNoMoreInteractions(watcher);

        // ------------------
        // Update old file, and see that it does *not* trigger.
        updated.set(false);
        reset(watcher);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onPathUpdate(fileCaptor.capture());

        write("really-old", oldFile);

        Thread.sleep(100L);

        verifyZeroInteractions(watcher);

        updated.set(false);
        reset(watcher);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onPathUpdate(fileCaptor.capture());

        newData = configMap.resolve("..2018-03");
        Files.createDirectories(newData);
        newFile = newData.resolve("test.file");
        write("new2", newFile);

        // ------------------
        // Same type of update again, and see that it still triggers.

        Files.createSymbolicLink(tmp, Paths.get(newData.getFileName().toString()));
        Files.move(tmp, data, REPLACE_EXISTING, ATOMIC_MOVE);

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onPathUpdate(configFile);
        assertThat(fileCaptor.getValue(), is(configFile));
        verifyNoMoreInteractions(watcher);

    }

    @Test
    public void testSymlinkFile_contentChange() throws IOException, InterruptedException {
        Path someFile = new File(temp.getRoot(), "test.txt").toPath();
        write("test", someFile);

        Path root = temp.newFolder("test")
                        .getCanonicalFile()
                        .getAbsoluteFile()
                        .toPath();
        Path someSym = root.resolve("link.txt");
        Files.createSymbolicLink(someSym, someFile);

        FileWatcher.Listener watcher  = mock(FileWatcher.Listener.class);
        FileWatcher.Listener watcher2 = mock(FileWatcher.Listener.class);

        sut.addWatcher(someSym, watcher);
        sut.addWatcher(watcher2);

        writeAndMove("other", someFile);

        Thread.sleep(1000L);

        verify(watcher).onPathUpdate(someSym);  // update as requested
        verify(watcher2).onPathUpdate(someFile);  // actual update
        verifyNoMoreInteractions(watcher, watcher2);
    }

    @Test
    public void testSymlinkFile_linkChange() throws IOException, InterruptedException {
        Path someFile = new File(temp.getRoot(), "test.txt").toPath();
        write("test", someFile);
        Path otherFile = new File(temp.getRoot(), "other.txt").toPath();
        write("other", someFile);

        Path root = temp.newFolder("test")
                        .getCanonicalFile()
                        .getAbsoluteFile()
                        .toPath();
        Path someSym = root.resolve("link.txt");
        Path tmpSym = new File(temp.getRoot(), "tmp").toPath();
        Files.createSymbolicLink(someSym, someFile);

        AtomicBoolean        updated    = new AtomicBoolean();
        ArgumentCaptor<Path> fileCaptor = ArgumentCaptor.forClass(Path.class);
        FileWatcher.Listener watcher    = mock(FileWatcher.Listener.class);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onPathUpdate(fileCaptor.capture());

        sut.addWatcher(someSym, watcher);

        Files.createSymbolicLink(tmpSym, otherFile);
        Files.move(tmpSym, someSym, REPLACE_EXISTING, ATOMIC_MOVE);

        // Make sure it is still a symbolic link.
        assertThat(Files.isSymbolicLink(someSym), is(true));

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onPathUpdate(someSym);
        assertThat(fileCaptor.getValue(), is(someSym));
    }


    @Test
    public void testFileArguments() throws IOException {
        try {
            sut.startWatching(null);
            fail("No exception on null file");
        } catch (IllegalArgumentException e) {
            assertEquals("Null file argument", e.getMessage());
        }

        try {
            sut.addWatcher((Path) null, null);
            fail("No exception on null file");
        } catch (IllegalArgumentException e) {
            assertEquals("Null file argument", e.getMessage());
        }

        try {

            sut.addWatcher(temp.newFile(), null);
            fail("No exception on null file");
        } catch (IllegalArgumentException e) {
            assertEquals("Null watcher argument", e.getMessage());
        }

        try {
            sut.stopWatching(null);
            fail("No exception on null file");
        } catch (IllegalArgumentException e) {
            assertEquals("Null file argument", e.getMessage());
        }

        sut.close();

        try {
            sut.startWatching(temp.newFile());
            fail("No exception on closed watcher");
        } catch (IllegalStateException e) {
            assertEquals("Starts to watch on closed FileWatcher", e.getMessage());
        }
    }

    @Test
    public void testCloseInterrupted() throws IOException, InterruptedException {
        sut.close();

        WatchService service = mock(WatchService.class);
        ExecutorService watcherService = mock(ExecutorService.class);
        ExecutorService callbackService = mock(ExecutorService.class);

        sut = new FileWatcher(service, watcherService, callbackService);

        reset(service, watcherService, callbackService);

        doThrow(new IOException("IO")).when(service).close();
        when(watcherService.isShutdown()).thenReturn(false);
        when(watcherService.awaitTermination(10, SECONDS)).thenThrow(new InterruptedException("WS"));
        when(callbackService.awaitTermination(10, SECONDS)).thenThrow(new InterruptedException("WS"));

        sut.close();

        verify(service).close();
        verify(watcherService).isShutdown();
        verify(watcherService).shutdown();
        verify(watcherService).awaitTermination(10, SECONDS);
        verify(callbackService).shutdown();
        verify(callbackService).awaitTermination(10, SECONDS);

        verifyNoMoreInteractions(service, watcherService, callbackService);

        assertThat(eventCaptor.getAllValues(), hasSize(3));
        assertThat(eventCaptor.getAllValues().get(0).toString(),
                   is("[ERROR] WatchService did not close"));
        assertThat(eventCaptor.getAllValues().get(1).toString(),
                   is("[ERROR] WatcherExecutor termination interrupted"));
        assertThat(eventCaptor.getAllValues().get(2).toString(),
                   is("[ERROR] CallbackExecutor termination interrupted"));

        reset(service, watcherService, callbackService);

        when(watcherService.isShutdown()).thenReturn(true);

        sut.close();

        verify(watcherService).isShutdown();
        verifyNoMoreInteractions(service, watcherService, callbackService);
    }

    @Test
    public void testCloseTimeout() throws IOException, InterruptedException {
        sut.close();

        WatchService service = mock(WatchService.class);
        ExecutorService watcherService = mock(ExecutorService.class);
        ExecutorService callbackService = mock(ExecutorService.class);

        sut = new FileWatcher(service, watcherService, callbackService);

        reset(service, watcherService, callbackService);

        when(watcherService.isShutdown()).thenReturn(false);
        when(watcherService.awaitTermination(10, SECONDS)).thenReturn(false);
        when(callbackService.awaitTermination(10, SECONDS)).thenReturn(false);

        sut.close();

        verify(service).close();
        verify(watcherService).isShutdown();
        verify(watcherService).shutdown();
        verify(watcherService).awaitTermination(10, SECONDS);
        verify(callbackService).shutdown();
        verify(callbackService).awaitTermination(10, SECONDS);

        verifyNoMoreInteractions(service, watcherService, callbackService);

        assertThat(eventCaptor.getAllValues(), hasSize(2));
        assertThat(eventCaptor.getAllValues().get(0).toString(),
                   is("[WARN] WatcherExecutor failed to terminate in 10 seconds"));
        assertThat(eventCaptor.getAllValues().get(1).toString(),
                   is("[WARN] CallbackExecutor failed to terminate in 10 seconds"));
    }

    private void write(String content, Path file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
            fos.write(content.getBytes(UTF_8));
            fos.flush();
            fos.getFD().sync();
        }
    }

    private void writeAndMove(String content, Path file) throws IOException {
        File tmp = temp.newFile();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(content.getBytes(UTF_8));
            fos.flush();
        }

        Files.move(tmp.toPath(), file, REPLACE_EXISTING, ATOMIC_MOVE);
    }
}
