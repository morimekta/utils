package net.morimekta.console;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.console.util.STTYMode;
import net.morimekta.console.util.STTYModeSwitcher;
import net.morimekta.util.Strings;

import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Testing the line input.
 */
public class InputLineTest {
    @Test
    public void testReadLine() throws IOException {
        STTYModeSwitcher switcher = mock(STTYModeSwitcher.class);
        InputStream in = mock(InputStream.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        when(switcher.didChangeMode()).thenReturn(true);
        when(switcher.getCurrentMode()).thenReturn(STTYMode.RAW);
        when(switcher.getBefore()).thenReturn(STTYMode.COOKED);

        Terminal terminal = new Terminal(in, out, null, switcher);

        InputLine li = new InputLine(terminal, "Test");

        verifyZeroInteractions(in);
        reset(in);

        assertArrayEquals(new byte[]{}, out.toByteArray());

        input(in,
              new Unicode('a'),
              Control.LEFT,
              new Unicode('b'),
              new Unicode(Char.CR));

        assertEquals("ba", li.readLine());

        assertEquals("Test: " +
                     "\\r\\033[KTest: a" +
                     "\\r\\033[KTest: a\\033[1D" +
                     "\\r\\033[KTest: ba\\033[1D",
                     Strings.escape(new String(out.toByteArray())));
    }

    private void input(InputStream in, Char ... chars) throws IOException {
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
