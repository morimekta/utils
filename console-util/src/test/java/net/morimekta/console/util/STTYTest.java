package net.morimekta.console.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the terminal size utility.
 */
public class STTYTest {
    @Test
    public void testGetTerminalSize() throws IOException, InterruptedException {
        Runtime runtime = mock(Runtime.class);
        Process process = mock(Process.class);

        String output = "65 129\n";

        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));

        STTY tty = new STTY(runtime);

        TerminalSize size = tty.getTerminalSize();

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verify(process).waitFor();
        verify(process).getErrorStream();
        verify(process).getInputStream();

        verifyNoMoreInteractions(runtime, process);

        assertEquals(65, size.rows);
        assertEquals(129, size.cols);
        assertEquals("tty(rows:65, cols:129)", size.toString());
    }

    @Test
    public void testIsInteractive() throws IOException, InterruptedException {
        Runtime runtime = mock(Runtime.class);
        Process process = mock(Process.class);

        String output = "65 129\n";

        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));

        STTY tty = new STTY(runtime);

        assertThat(tty.isInteractive(), is(true));

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verify(process).waitFor();
        verify(process).getErrorStream();
        verify(process).getInputStream();

        verifyNoMoreInteractions(runtime, process);
    }

    @Test
    public void testIsInteractive_not() throws IOException {
        Runtime runtime = mock(Runtime.class);

        when(runtime.exec(any(String[].class))).thenThrow(new IOException());

        STTY tty = new STTY(runtime);

        assertThat(tty.isInteractive(), is(false));

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verifyNoMoreInteractions(runtime);
    }
}
