package net.morimekta.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.morimekta.util.FileUtil.readCanonicalPath;
import static net.morimekta.util.FileUtil.replaceSymbolicLink;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FileUtilTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testReadCanonicalPath() throws IOException {
        Path dot = new File(".").getAbsoluteFile()
                                .getCanonicalFile()
                                .toPath();
        Path root = tmp.getRoot()
                       .getCanonicalFile()
                       .getAbsoluteFile()
                       .toPath();

        assertThat(readCanonicalPath(Paths.get(".")), is(dot));

        Path target1 = Files.createDirectory(root.resolve("target1"));
        Path target2 = Files.createDirectory(root.resolve("target2"));

        Path link = root.resolve("link");
        replaceSymbolicLink(link, target1);
        assertThat(readCanonicalPath(link), is(target1));
        replaceSymbolicLink(link, target2);

        assertThat(readCanonicalPath(link), is(target2));

        Path link2 = root.resolve("link2");
        replaceSymbolicLink(link2, Paths.get("target1"));
        assertThat(readCanonicalPath(link2), is(target1));

        Path link3 = target1.resolve("link3");
        Path target3 = Paths.get("../target2");
        replaceSymbolicLink(link3, target3);
        assertThat(readCanonicalPath(link3), is(target2));
    }

    @Test
    public void testReadCanonicalPath_fail() throws IOException {
        Path root = tmp.getRoot()
                       .getCanonicalFile()
                       .getAbsoluteFile()
                       .toPath();

        try {
            readCanonicalPath(root.resolve("../../../../../../../.."));
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Parent of root does not exist!"));
        }

        try {
            readCanonicalPath(Paths.get("/.."));
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Parent of root does not exist!"));
        }
    }

    @Test
    public void testReplaceSymbolicLink() throws IOException {
        Path root = tmp.getRoot()
                       .getCanonicalFile()
                       .getAbsoluteFile()
                       .toPath();

        Path dir = tmp.newFolder("dir").toPath();
        Path dir2 = tmp.newFolder("dir2").toPath();

        Path dirLink = root.resolve("link1");
        replaceSymbolicLink(dirLink, dir);
        assertThat(readCanonicalPath(dirLink), is(dir));
        replaceSymbolicLink(dirLink, dir2);
        assertThat(readCanonicalPath(dirLink), is(dir2));

        Path file = tmp.newFile("file").toPath();
        Path file2 = tmp.newFile("file2").toPath();
        Path fileLink = root.resolve("link2");
        replaceSymbolicLink(fileLink, file);
        assertThat(readCanonicalPath(fileLink), is(file));
        replaceSymbolicLink(fileLink, file2);
        assertThat(readCanonicalPath(fileLink), is(file2));
    }

    @Test
    public void testReplaceSymbolicLinkFail() throws IOException {
        Path root = tmp.getRoot()
                       .getCanonicalFile()
                       .getAbsoluteFile()
                       .toPath();

        Path file = tmp.newFile("file1").toPath();
        Path target = root.resolve("target1");
        try {
            replaceSymbolicLink(file, target);
            fail("no exception");
        } catch (NotLinkException e) {
            assertThat(e.getMessage(), is(root.toString() + "/file1 is not a symbolic link"));
        }
    }
}
