package net.morimekta.console.terminal;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.chr.Unicode;
import net.morimekta.console.terminal.InputPassword;
import net.morimekta.console.terminal.Terminal;
import net.morimekta.console.test_utils.TerminalTestUtils;
import net.morimekta.console.util.STTY;
import net.morimekta.util.Strings;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import static net.morimekta.console.test_utils.TerminalTestUtils.mockInput;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Testing the line input.
 */
public class InputPasswordTest {
    @Test
    public void testReadLine() throws IOException {
        InputStream in = mock(InputStream.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Terminal terminal = TerminalTestUtils.getTerminal(new STTY(), out, in);

        InputPassword li = new InputPassword(terminal, "Test");

        verifyZeroInteractions(in);
        reset(in);

        assertArrayEquals(new byte[]{}, out.toByteArray());

        mockInput(in,
                  new Unicode('a'),
                  Control.LEFT,
                  new Unicode('b'),
                  new Unicode(Char.CR));

        assertEquals("ba", li.readPassword());

        assertEquals("Test: " +
                     "\\r\\033[KTest: *" +
                     "\\r\\033[KTest: *\\033[1D" +
                     "\\r\\033[KTest: **\\033[1D",
                     Strings.escape(new String(out.toByteArray())));
    }

    @Test
    public void testEOF() throws IOException {
        Terminal terminal = TerminalTestUtils.getTerminal(new STTY());

        try {
            new InputPassword(terminal, "test").readPassword();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: End of input.", e.getMessage());
        }
    }

    @Test
    public void testMovements() throws IOException {
        Terminal terminal = TerminalTestUtils.getTerminal(new STTY(),
                                                          "aba",
                                                          Control.LEFT,
                                                          Control.LEFT,
                                                          "cd",
                                                          Control.CTRL_LEFT,
                                                          Char.DEL,
                                                          "e",
                                                          Control.RIGHT,
                                                          "f",
                                                          Control.CTRL_RIGHT,
                                                          "g",
                                                          Char.CR);

        assertEquals("acebfga", new InputPassword(terminal, "test").readPassword());
    }
}
