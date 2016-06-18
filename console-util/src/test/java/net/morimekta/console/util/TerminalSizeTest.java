package net.morimekta.console.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for the terminal size utility.
 */
public class TerminalSizeTest {
    @Test
    public void testGetTerminalSize() throws IOException, InterruptedException {
        Runtime runtime = mock(Runtime.class);
        Process process = mock(Process.class);

        String output = "65 129\n";

        when(runtime.exec(any(String[].class))).thenReturn(process);
        when(process.waitFor()).thenReturn(0);
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));

        TerminalSize size = TerminalSize.get(runtime);

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty size </dev/tty"});
        verify(process).waitFor();
        verify(process).getErrorStream();
        verify(process).getInputStream();

        verifyNoMoreInteractions(runtime, process);

        assertEquals(65, size.rows);
        assertEquals(129, size.cols);
        assertEquals("tty(rows:65, cols:129)", size.toString());
    }
}
