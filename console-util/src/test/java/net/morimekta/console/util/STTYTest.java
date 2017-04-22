package net.morimekta.console.util;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the terminal size utility.
 */
public class STTYTest {
    private Runtime runtime;
    private Process process;
    private String output;

    @Before
    public void setUp() throws IOException, InterruptedException {
        output = "65 129\n";

        runtime = mock(Runtime.class);
        process = mock(Process.class);
    }

    @Test
    public void testInteractive() throws InterruptedException, IOException {
        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream())
                .thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(process.getInputStream())
                .thenReturn(new ByteArrayInputStream(output.getBytes()));

        STTY tty = new STTY(runtime);

        assertThat(tty.toString(),
                   is("STTY{interactive=true, tty=tty(rows:65, cols:129), mode=COOKED}"));
        assertThat(tty.isInteractive(), is(true));
        assertThat(tty.getTerminalSize(), is(new TerminalSize(65, 129)));
        assertThat(tty.getTerminalSize().hashCode(), is(not(new TerminalSize(12, 13).hashCode())));

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verify(process).waitFor();
        verify(process).getErrorStream();
        verify(process).getInputStream();
        verifyNoMoreInteractions(runtime, process);
    }

    @Test
    public void testNonInteractive() throws IOException, InterruptedException {
        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(1);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("Unknown device /dev/tty\n".getBytes()));

        STTY tty = new STTY(runtime);

        assertThat(tty.isInteractive(), is(false));
        assertThat(tty.toString(),
                   is("STTY{interactive=false, mode=COOKED}"));
        try {
            tty.getTerminalSize();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getCause().getMessage(), is("Unknown device /dev/tty"));
        }

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verify(process).waitFor();
        verify(process).getErrorStream();
        verifyNoMoreInteractions(runtime, process);
    }

    @Test
    public void testSetSTTYMode() throws IOException, InterruptedException {
        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        STTY tty = new STTY(runtime);

        try (STTYModeSwitcher switcher = tty.setSTTYMode(STTYMode.RAW)) {
            assertThat(switcher.didChangeMode(), is(true));
            assertThat(switcher.getMode(), is(STTYMode.RAW));
            assertThat(switcher.getCurrentMode(), is(STTYMode.RAW));
        }

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty raw -echo </dev/tty"});
        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty -raw echo </dev/tty"});
        verify(process, times(2)).waitFor();
        verify(process, times(2)).getErrorStream();
    }
}
