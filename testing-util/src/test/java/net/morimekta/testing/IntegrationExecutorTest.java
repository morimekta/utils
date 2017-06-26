package net.morimekta.testing;

import net.morimekta.util.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.concurrent.ExecutorService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the integration executor.
 */
public class IntegrationExecutorTest {
    @Rule
    public  TemporaryFolder temporaryFolder;
    private Runtime         runtime;
    private ExecutorService executor;
    private File            integration;

    @Before
    public void setUp() throws IOException {
        temporaryFolder = new TemporaryFolder();
        temporaryFolder.create();

        integration = temporaryFolder.newFile("integration.jar");

        try (FileOutputStream out = new FileOutputStream(integration);
             InputStream in = getClass().getResourceAsStream("/integration.zip")) {
            IOUtils.copy(in, out);
            out.flush();
        }
    }

    @After
    public void tearDown() {
        temporaryFolder.delete();
    }

    @Test
    public void testRun() throws IOException {
        IntegrationExecutor sut = new IntegrationExecutor(integration);

        assertEquals(0, sut.run("a", "b"));
        assertEquals("", sut.getError());
        assertEquals("len: 2\n" +
                     "+ a\n" +
                     "+ b\n", sut.getOutput());
    }

    @Test
    public void testRun_withDelay() throws IOException {
        IntegrationExecutor sut = new IntegrationExecutor(integration);

        long now = Clock.systemUTC().millis();

        assertEquals(0, sut.run("sleep", "100"));
        assertEquals("- sleep\n", sut.getError());
        assertEquals("len: 2\n" +
                     "+ 100\n", sut.getOutput());

        long then = Clock.systemUTC().millis();

        assertTrue("Time spent: " + then + " > " + (now + 100) + "", then > (now + 100));
    }

    @Test
    public void testRun_deadlineExceeded() throws IOException {
        IntegrationExecutor sut = new IntegrationExecutor(integration);
        sut.setDeadlineMs(100);

        try {
            sut.run("sleep", "10000");
            fail("No exception on deadline exceeded.");
        } catch (IOException e) {
            assertEquals("Process took too long: java -jar " + integration.getAbsolutePath() + " sleep 10000", e.getMessage());
        }
    }

    @Test
    public void testRun_withInput() throws IOException {
        IntegrationExecutor sut = new IntegrationExecutor(integration);

        ByteArrayInputStream in = new ByteArrayInputStream("one step ahead ;)".getBytes(UTF_8));
        sut.setInput(in);

        assertEquals(0, sut.run("cat"));
        assertEquals("len: 1\n" +
                     "one step ahead ;)\n" +
                     "+ cat\n", sut.getOutput());
    }

    @Test
    public void testFindMavenTargetJar() {
        try {
            new IntegrationExecutor("src", "does-not-exist.1234567890.jar");
            fail("No exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("No such jar file: does-not-exist.1234567890.jar"));
        }
    }
}
