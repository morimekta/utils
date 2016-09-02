package net.morimekta.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class FileWatcherTest {
    public TemporaryFolder temp;

    private FileWatcher sut;

    @Before
    public void setUp() throws IOException {
        Logger logger = (Logger) LoggerFactory.getLogger(FileWatcher.class);
        logger.setLevel(Level.ERROR);

        temp = new TemporaryFolder();
        temp.create();

        sut = new FileWatcher();
    }

    @After
    public void tearDown() throws IOException {
        temp.delete();
        sut.close();
    }

    @Test
    public void testSimpleWatch() throws IOException, InterruptedException {
        File test1 = temp.newFile("test1");
        File test2 = temp.newFile("test2");
        File test3 = new File(temp.getRoot(), "test3");
        temp.newFile("test4");
        File test5 = new File(temp.getRoot(), "test5");

        File subDir = temp.newFolder();
        subDir.mkdirs();
        File subFile = new File(subDir, "subFile");
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
        File file1 = temp.newFile();
        File file2 = temp.newFile();

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

        FileWatcher.Watcher w1 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w2 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w3 = mock(FileWatcher.Watcher.class);
        FileWatcher.Watcher w4 = mock(FileWatcher.Watcher.class);

        sut.addWatcher(w1);
        sut.addWatcher(w2);

        sut2.addWatcher(w3);
        sut2.addWatcher(w4);

        File f1 = temp.newFile();
        File f2 = temp.newFile();
        File f12 = temp.newFile();

        sut.startWatching(f1);
        sut2.startWatching(f2);
        sut.startWatching(f12);
        sut2.startWatching(f12);

        // The "writeAndMove" version should have an atomic "CREATE" event, so
        // there is no intermediate "partially written" state to count.
        writeAndMove("a", f1);
        writeAndMove("b", f2);
        writeAndMove("c", f12);

        sleep(100L);

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

        sut.removeWatcher(w2);
        sut2.removeWatcher(w4);

        sleep(100L);

        writeAndMove("d", f1);
        writeAndMove("e", f2);
        writeAndMove("f", f12);

        sleep(100L);

        verify(w1).onFileUpdate(eq(f1));
        verify(w1).onFileUpdate(eq(f12));

        verify(w3).onFileUpdate(eq(f2));
        verify(w3).onFileUpdate(eq(f12));

        verifyNoMoreInteractions(w1, w2, w3, w4);
    }

    @Test
    public void testMultipleDirectories() throws IOException, InterruptedException {
        File dir1 = temp.newFolder("dir1");
        File dir2 = temp.newFolder("dir2");

        File f1 = new File(dir1, "f1");
        File f2 = new File(dir2, "f2");

        FileWatcher.Watcher watcher = mock(FileWatcher.Watcher.class);
        sut.addWatcher(watcher);
        sut.startWatching(f1);
        sut.startWatching(new File(f1.toString()));
        sut.startWatching(f2);

        writeAndMove("1", f1);
        writeAndMove("2", f2);

        sleep(100L);

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
