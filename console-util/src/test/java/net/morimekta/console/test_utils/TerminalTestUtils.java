package net.morimekta.console.test_utils;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.terminal.Terminal;
import net.morimekta.console.util.STTY;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import org.mockito.stubbing.OngoingStubbing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Utilities for testing of terminal usage.
 */
public class TerminalTestUtils {
    public static Terminal getTerminal(STTY tty, Object... in) throws IOException {
        return getTerminal(tty, new ByteArrayOutputStream(), in);
    }

    public static Terminal getTerminal(STTY tty, OutputStream out, Object... in) throws IOException {
        return getTerminal(tty, out, new ByteArrayInputStream(CharUtil.inputBytes(in)));
    }

    public static Terminal getTerminal(STTY tty, OutputStream out, InputStream in) {
        STTYModeSwitcher switcher = mock(STTYModeSwitcher.class);
        when(switcher.didChangeMode()).thenReturn(true);
        when(switcher.getCurrentMode()).thenReturn(STTYMode.RAW);
        when(switcher.getBefore()).thenReturn(STTYMode.COOKED);

        return new Terminal(tty,
                            in,
                            out,
                            null,
                            switcher);
    }

    public static void mockInput(InputStream in, Char ... chars) throws IOException {
        reset(in);

        // Always continue reading.
        when(in.available()).thenReturn(1);

        OngoingStubbing<Integer> os = when(in.read());
        for (Char ch : chars) {
            for (char c : ch.toString().toCharArray()) {
                os = os.thenReturn((int) c);
            }
        }
        os.thenReturn(-1);
    }
}
