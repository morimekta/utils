package net.morimekta.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TemporaryAssetFolderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void testTempAssetFolder() throws IOException {
        Path tempDir;
        try (TemporaryAssetFolder taf = new TemporaryAssetFolder(tmp.getRoot().toPath())) {
            tempDir = taf.getPath();
            Files.write(taf.getPath().resolve("foo1"), "bar1".getBytes(UTF_8));
            Files.write(taf.getPath().resolve("foo2"), "bar2".getBytes(UTF_8));
            Files.createSymbolicLink(taf.getPath().resolve("foo3"), Paths.get("foo2"));
            Files.createDirectories(taf.getPath().resolve("foo4"));
            Files.write(taf.getPath().resolve("foo4/foo"), "bar4".getBytes(UTF_8));

            long num = Files.list(tmp.getRoot().toPath()).count();
            assertThat(num, is(1L));

            num = Files.list(taf.getPath()).count();
            assertThat(num, is(4L));
        }

        long num = Files.list(tmp.getRoot().toPath()).count();
        assertThat(num, is(0L));
        assertThat(Files.exists(tempDir), is(false));
    }

    @Test
    public void testTempAssetFolder_File() throws IOException {
        Path tempDir;
        try (TemporaryAssetFolder taf = new TemporaryAssetFolder(tmp.getRoot())) {
            tempDir = taf.getPath();
            Files.write(taf.getPath().resolve("foo1"), "bar1".getBytes(UTF_8));
            Files.write(taf.getPath().resolve("foo2"), "bar2".getBytes(UTF_8));
            Files.createSymbolicLink(taf.getPath().resolve("foo3"), Paths.get("foo2"));
            Files.createDirectories(taf.getPath().resolve("foo4"));
            Files.write(taf.getPath().resolve("foo4/foo"), "bar4".getBytes(UTF_8));

            long num = Files.list(tmp.getRoot().toPath()).count();
            assertThat(num, is(1L));

            num = Files.list(taf.getPath()).count();
            assertThat(num, is(4L));
        }

        long num = Files.list(tmp.getRoot().toPath()).count();
        assertThat(num, is(0L));
        assertThat(Files.exists(tempDir), is(false));
    }
}
