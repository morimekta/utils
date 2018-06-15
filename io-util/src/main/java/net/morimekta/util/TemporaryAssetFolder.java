package net.morimekta.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A temporary asset folder where an arbitrary number of files can be added
 * to and should guarantee complete cleanup on close. Example usage:
 *
 * <pre>{@code
 * try (TemporaryAssetFolder tmp = new TemporaryAssetFolder(base)) {
 *     // do stuff
 * }
 * // is all deleted.
 * }</pre>
 *
 * This will create a temp folder into the base directory for each run, where
 * any number of files or sub-folders can be added. All of it will be removed
 * immediately on close.
 */
public class TemporaryAssetFolder implements Closeable {
    private final Path path;

    public TemporaryAssetFolder(File baseTempDirectory) throws IOException {
        this(baseTempDirectory.toPath());
    }

    public TemporaryAssetFolder(Path baseTempDirectory) throws IOException {
        path = Files.createTempDirectory(baseTempDirectory, "tmp");
    }

    public Path getPath() {
        return path;
    }

    public File getFile() {
        return getPath().toFile();
    }

    @Override
    public void close() throws IOException {
        FileUtil.deleteRecursively(path);
    }
}
