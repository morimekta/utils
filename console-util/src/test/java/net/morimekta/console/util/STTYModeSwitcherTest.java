package net.morimekta.console.util;

import net.morimekta.console.test_utils.ConsoleWatcher;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Testing the TTY Mode switcher.
 */
public class STTYModeSwitcherTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testSwitch() throws IOException, InterruptedException {
        Runtime runtime = mock(Runtime.class);
        Process process1 = mock(Process.class);
        Process process2 = mock(Process.class);

        when(runtime.exec(any(String[].class))).thenReturn(process1);
        when(process1.waitFor()).thenReturn(0);
        when(process1.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

        try (STTYModeSwitcher sms = new STTYModeSwitcher(STTYMode.RAW, runtime)){
            assertEquals(STTYMode.RAW, sms.getMode());
            assertEquals(STTYMode.COOKED, sms.getBefore());
            assertTrue(sms.didChangeMode());
            assertEquals(STTYMode.RAW, sms.getCurrentMode());
            assertEquals(STTYMode.RAW, STTYModeSwitcher.currentMode());

            verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty raw -echo </dev/tty"});
            verify(process1).waitFor();
            verify(process1).getErrorStream();

            verifyNoMoreInteractions(runtime);
            verifyNoMoreInteractions(process1);

            reset(runtime, process1);

            when(runtime.exec(any(String[].class))).thenReturn(process2);
            when(process2.waitFor()).thenReturn(0);
            when(process2.getErrorStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));
        }

        assertEquals(STTYMode.COOKED, STTYModeSwitcher.currentMode());

        verify(runtime).exec(new String[]{"/bin/sh", "-c", "stty -raw echo </dev/tty"});
        verify(process2).waitFor();
        verify(process2).getErrorStream();
        verifyNoMoreInteractions(runtime);
        verifyNoMoreInteractions(process2);

        verifyZeroInteractions(process1);
    }
}
