package net.morimekta.console;

import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import net.morimekta.util.Strings;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by morimekta on 6/18/16.
 */
public class TerminalTest {
    @Test
    public void testConfirm() throws IOException {
        STTYModeSwitcher switcher = mock(STTYModeSwitcher.class);
        InputStream in = mock(InputStream.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(switcher.didChangeMode()).thenReturn(true);
        when(switcher.getBefore()).thenReturn(STTYMode.COOKED);

        Terminal terminal = new Terminal(in, out, null, switcher);

        verify(switcher).didChangeMode();
        verify(switcher).getBefore();
        verifyNoMoreInteractions(switcher);
        verifyZeroInteractions(in);

        assertArrayEquals(new byte[]{}, out.toByteArray());

        reset(switcher, in);

        when(switcher.getCurrentMode()).thenReturn(STTYMode.RAW);

        when(in.read()).thenReturn((int) 'y');

        assertTrue(terminal.confirm("Test"));

        verify(in).read();
        verify(switcher).getCurrentMode();

        verifyNoMoreInteractions(in);
        verifyNoMoreInteractions(switcher);

        assertEquals("Test [Y/n]: Yes.", Strings.escape(new String(out.toByteArray())));
    }

    @Test
    public void testLineReader() {
        STTYModeSwitcher switcher = mock(STTYModeSwitcher.class);
        InputStream in = mock(InputStream.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(switcher.didChangeMode()).thenReturn(true);
        when(switcher.getCurrentMode()).thenReturn(STTYMode.RAW);
        when(switcher.getBefore()).thenReturn(STTYMode.COOKED);

        Terminal terminal = new Terminal(in, out, null, switcher);

        out.reset();
        terminal.info("test");
        assertEquals("\\033[32m[info]\\033[00m test", Strings.escape(new String(out.toByteArray())));

        out.reset();
        terminal.warn("test");
        assertEquals("\\r\\n\\033[33m[warn]\\033[00m test", Strings.escape(new String(out.toByteArray())));

        out.reset();
        terminal.error("test");
        assertEquals("\\r\\n\\033[31m[error]\\033[00m test", Strings.escape(new String(out.toByteArray())));

        out.reset();
        terminal.fatal("test");
        assertEquals("\\r\\n\\033[01;31m[FATAL]\\033[00m test", Strings.escape(new String(out.toByteArray())));
    }
}
