package net.morimekta.testing;

import net.morimekta.util.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Utility for help with managing resources during tests.
 */
public class ResourceUtils {
    /**
     * Copy a resource to the target directory. The resource file retains it's
     * name. If the target is a file, the file is overwritten with the content
     * of the resource file.
     *
     * @param resource The resource path.
     * @param target Target file or directory.
     * @return File created and written to.
     */
    public static File copyResourceTo(String resource, File target) {
        if (!target.exists()) {
            fail("Trying to copy resource '" + resource + "' to non-existing directory: '" + target + "'");
        }
        if (target.isDirectory()) {
            int i = resource.lastIndexOf('/');
            target = new File(target, resource.substring(i + 1));
        }

        try (FileOutputStream fos = new FileOutputStream(target);
             BufferedOutputStream out = new BufferedOutputStream(fos);
             InputStream in = getResourceAsStream(resource)) {
            IOUtils.copy(in, out);
            out.flush();
            fos.getFD().sync();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return target;
    }

    /**
     * Write the file content to the target file.
     *
     * @param content The file content.
     * @param target The file to write to.
     * @return File written to.
     */
    public static File writeContentTo(String content, File target) {
        assertNotNull("Content is null", content);
        assertNotNull("Target file is null", target);
        try (FileOutputStream fos = new FileOutputStream(target);
             BufferedOutputStream out = new BufferedOutputStream(fos)) {
            out.write(content.getBytes(UTF_8));
            out.flush();
            fos.getFD().sync();
            return target;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Get the resource as a buffered input stream. Fail if no such resource exists.
     *
     * @param resource The resource to get.
     * @return The buffered input stream.
     */
    public static BufferedInputStream getResourceAsStream(String resource) {
        assertNotNull("Given null resource", resource);
        InputStream in = ResourceUtils.class.getResourceAsStream(resource);
        assertNotNull("Trying to read non-existing resource " + resource, in);
        return new BufferedInputStream(in);
    }

    /**
     * Get the resource content as a byte array. Fail if the resource does not exist,
     * or if we failed to read the resource file.
     *
     * @param resource The resource to read.
     * @return The resource content as byte array.
     */
    public static byte[] getResourceAsBytes(String resource) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InputStream in = getResourceAsStream(resource)) {
            IOUtils.copy(in, baos);
        } catch (IOException e) {
            fail("Failed to read resource '" + resource + "': " + e.getMessage());
        }

        return baos.toByteArray();
    }

    /**
     * Get the resource content as a byte buffer. Fail if the resource does not exist,
     * or if we failed to read the resource file.
     *
     * @param resource The resource to read.
     * @return The resource content as byte buffer.
     */
    public static ByteBuffer getResourceAsByteBuffer(String resource) {
        return ByteBuffer.wrap(getResourceAsBytes(resource));
    }

    /**
     * Get the resource content as a string. Fail if the resource does not exist,
     * or if we failed to read the resource file.
     *
     * @param resource The resource to read.
     * @return The resource content as string.
     */
    public static String getResourceAsString(String resource) {
        return new String(getResourceAsBytes(resource), StandardCharsets.UTF_8);
    }

    // PRIVATE constructor defeats instantiation.
    private ResourceUtils() {}
}
