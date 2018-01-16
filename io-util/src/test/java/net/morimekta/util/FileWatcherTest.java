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
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
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

/**
 * TODO(steineldar): Make a proper class description.
 */
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
    public void tearDown() throws IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(FileWatcher.class);
        logger.detachAppender(appender);

        sut.close();
    }

    @Test
    public void testSimpleWatch() throws IOException, InterruptedException {
        File test1 = temp.newFile("test1").getCanonicalFile();
        File test2 = temp.newFile("test2").getCanonicalFile();
        File test3 = new File(temp.getRoot(), "test3").getCanonicalFile();
        temp.newFile("test4");
        File test5 = new File(temp.getRoot(), "test5").getCanonicalFile();

        File subDir = temp.newFolder();
        subDir.mkdirs();
        File subFile = new File(subDir, "subFile").getCanonicalFile();
        subFile.createNewFile();

        write("1", test1);
        write("2", test2);
        write("3", subFile);

        sleep(100L);

        sut.startWatching(test1);  // written to.
        sut.startWatching(test3);  // copied to
        sut.startWatching(test5);  // no event.

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);

        sut.addWatcher(watcher);

        write("4", test1);
        write("5", test2);  // no event.
        subFile.renameTo(test3);

        sleep(100L);

        // NOTE: Since a write may be thread-interrupted it may arrive as a
        // set of updates on the other side.

        // no callback on write to test2.
        verify(watcher, atLeastOnce()).onFileUpdate(eq(test1));
        verify(watcher, atLeastOnce()).onFileUpdate(eq(test3));
        verifyNoMoreInteractions(watcher);
    }

    @Test
    public void testStopWatching() throws IOException, InterruptedException {
        File file1 = temp.newFile().getCanonicalFile();
        File file2 = temp.newFile().getCanonicalFile();

        sut.startWatching(file1);
        sut.startWatching(file2);

        writeAndMove("a", file1);
        writeAndMove("b", file2);

        sleep(100L);

        sut.stopWatching(file2);

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);
        sut.addWatcher(watcher);

        writeAndMove("c", file2);
        writeAndMove("d", file1);

        sleep(100L);

        verify(watcher).onFileUpdate(file1);
        verifyNoMoreInteractions(watcher);
    }

    @Test
    public void testWeakWatchers() throws IOException {
        File file1 = temp.newFile().getCanonicalFile();
        writeAndMove("a", file1);

        sut.startWatching(file1);

        AtomicBoolean watcherCalled = new AtomicBoolean();
        FileWatcher.Watcher watcher = f -> watcherCalled.set(true);

        FileWatcher.Watcher innerWatcher = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher weakWatcher = f -> innerWatcher.onFileUpdate(f);

        sut.addWatcher(watcher);
        sut.weakAddWatcher(weakWatcher);

        System.gc();

        writeAndMove("b", file1);

        await().atMost(Duration.ONE_MINUTE).untilTrue(watcherCalled);
        watcherCalled.set(false);

        verify(innerWatcher).onFileUpdate(file1);

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
        AtomicBoolean called = new AtomicBoolean();
        FileWatcher.Watcher w0 = f -> called.set(true);
        sut.weakAddWatcher(w0);

        File file1 = temp.newFile().getCanonicalFile();
        sut.startWatching(file1);
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
        FileWatcher sut2 = new FileWatcher();
        AtomicInteger called = new AtomicInteger(0);
        FileWatcher.Watcher w0 = f -> called.incrementAndGet();
        FileWatcher.Watcher w1 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w2 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w3 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w4 = mock(FileWatcher.Watcher.class);

        sut.addWatcher(w0);
        sut.addWatcher(w1);
        sut.addWatcher(w2);

        sut2.addWatcher(w0);
        sut2.addWatcher(w3);
        sut2.addWatcher(w4);

        File f1 = temp.newFile().getCanonicalFile();
        File f2 = temp.newFile().getCanonicalFile();
        File f12 = temp.newFile().getCanonicalFile();
        f12.delete();

        sut.startWatching(f1);
        sut2.startWatching(f2);
        sut.startWatching(f12);
        sut2.startWatching(f12);

        // The "writeAndMove" version should have an atomic "CREATE" event, so
        // there is no intermediate "partially written" state to count.
        writeAndMove("a", f1);
        writeAndMove("b", f2);
        writeAndMove("c", f12);

        await().atMost(1, MINUTES).untilAtomic(called, is(4));
        called.set(0);

        verify(w1).onFileUpdate(eq(f1));
        verify(w1).onFileUpdate(eq(f12));
        verify(w2).onFileUpdate(eq(f1));
        verify(w2).onFileUpdate(eq(f12));

        verify(w3).onFileUpdate(eq(f2));
        verify(w3).onFileUpdate(eq(f12));
        verify(w4).onFileUpdate(eq(f2));
        verify(w4).onFileUpdate(eq(f12));

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

        verify(w1).onFileUpdate(eq(f1));
        verify(w1).onFileUpdate(eq(f12));

        verify(w3).onFileUpdate(eq(f2));
        verify(w3).onFileUpdate(eq(f12));

        verifyNoMoreInteractions(w1, w2, w3, w4);
    }

    @Test
    public void testMultipleDirectories() throws IOException, InterruptedException {
        AtomicInteger called = new AtomicInteger(0);
        FileWatcher.Watcher w0 = f -> called.incrementAndGet();

        File dir1 = temp.newFolder("dir1");
        File dir2 = temp.newFolder("dir2");

        File f1 = new File(dir1, "f1").getCanonicalFile();
        File f2 = new File(dir2, "f2").getCanonicalFile();

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);
        sut.addWatcher(w0);
        sut.addWatcher(watcher);
        sut.startWatching(f1);
        sut.startWatching(new File(f1.toString()));
        sut.startWatching(f2);

        writeAndMove("1", f1);
        writeAndMove("2", f2);

        await().atMost(1, MINUTES).untilAtomic(called, is(2));

        verify(watcher).onFileUpdate(f1);
        verify(watcher).onFileUpdate(f2);
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

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);

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
        File root = temp.getRoot()
                        .getCanonicalFile()
                        .getAbsoluteFile();
        File someDir = new File(root, "test");
        Files.createDirectories(someDir.toPath());
        File someSym = new File(root, "link");
        File file = new File(someDir, "test.txt");
        File toListen = new File(someSym, "test.txt");
        write("test", file);

        Files.createSymbolicLink(someSym.toPath(), someDir.toPath());

        AtomicBoolean updated = new AtomicBoolean();
        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onFileUpdate(fileCaptor.capture());

        sut.addWatcher(watcher);
        sut.startWatching(toListen);

        writeAndMove("other", file);

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onFileUpdate(file);
        assertThat(fileCaptor.getValue(), is(file));
    }

    @Test
    public void testSymlinkFile_contentChange() throws IOException, InterruptedException {
        File someFile = new File(temp.getRoot(), "test.txt");
        write("test", someFile);

        File root = temp.newFolder("test")
                        .getCanonicalFile()
                        .getAbsoluteFile();
        File someSym = new File(root, "link.txt");
        Files.createSymbolicLink(someSym.toPath(), someFile.toPath());

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);

        sut.addWatcher(watcher);
        sut.startWatching(someSym);

        writeAndMove("other", someFile);

        Thread.sleep(1000L);

        // does not trigger, only change of the link itself will trigger here.
        verifyZeroInteractions(watcher);
    }

    @Test
    public void testSymlinkFile_linkChange() throws IOException, InterruptedException {
        File someFile = new File(temp.getRoot(), "test.txt");
        write("test", someFile);
        File otherFile = new File(temp.getRoot(), "other.txt");
        write("other", someFile);

        File root = temp.newFolder("test")
                        .getCanonicalFile()
                        .getAbsoluteFile();
        File someSym = new File(root, "link.txt");
        File tmpSym = new File(temp.getRoot(), "tmp");
        Files.createSymbolicLink(someSym.toPath(), someFile.toPath());

        AtomicBoolean updated = new AtomicBoolean();
        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);
        doAnswer(i -> {
            updated.set(true);
            return null;
        }).when(watcher).onFileUpdate(fileCaptor.capture());

        sut.addWatcher(watcher);
        sut.startWatching(someSym);

        Files.createSymbolicLink(tmpSym.toPath(), otherFile.toPath());
        Files.move(tmpSym.toPath(), someSym.toPath(),
                   StandardCopyOption.REPLACE_EXISTING,
                   StandardCopyOption.ATOMIC_MOVE);

        // Make sure it is still a symbolic link.
        assertThat(Files.isSymbolicLink(someSym.toPath()), is(true));

        waitAtMost(Duration.ONE_SECOND).untilTrue(updated);

        verify(watcher).onFileUpdate(someSym);
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

    private void write(String content, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content.getBytes(UTF_8));
            fos.flush();
            fos.getFD().sync();
        }
    }

    private void writeAndMove(String content, File file) throws IOException {
        File tmp = temp.newFile();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            fos.write(content.getBytes(UTF_8));
            fos.flush();
        }

        tmp.renameTo(file);
    }
}
