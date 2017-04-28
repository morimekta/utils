package net.morimekta.console.terminal;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.test_utils.ConsoleWatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Testing the line input.
 */
public class InputPasswordTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();
    private Terminal terminal;

    @Before
    public void setUp() {
        terminal = new Terminal(console.tty());
    }

    @Test
    public void testReadLine() throws IOException {
        InputPassword li = new InputPassword(terminal, "Test");

        console.setInput('a',
                         Control.LEFT,
                         'b',
                         Char.CR);

        assertThat(li.readPassword(), is("ba"));
        assertThat(console.output(),
                   is("Test: " +
                      "\r\033[KTest: *" +
                      "\r\033[KTest: *\033[1D" +
                      "\r\033[KTest: **\033[1D"));
    }

    @Test
    public void testEOF() throws IOException {
        try {
            new InputPassword(terminal, "test").readPassword();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: End of input.", e.getMessage());
        }
    }

    @Test
    public void testMovements() throws IOException {
        console.setInput(
                "aba",
                Control.LEFT,
                'b',
                Control.HOME,
                '-',
                Control.RIGHT,
                Control.END,
                Char.DEL,
                Control.LEFT,
                "e\t",
                Control.DELETE,
                "fg",
                Char.CR);

        assertThat(new InputPassword(terminal, "test").readPassword(),
                   is("-abefg"));
    }

    @Test
    public void testInterrupt() {
        console.setInput("ab", Char.ESC);
        try {
            new InputPassword(terminal, "test").readPassword();
            fail();
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(),
                       is("java.io.IOException: User interrupted: <ESC>"));
        }
    }
}
