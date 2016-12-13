package net.morimekta.console;

import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.test_utils.TerminalTestUtils;
import net.morimekta.console.util.STTY;
import net.morimekta.console.util.TerminalSize;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing the line input.
 */
public class InputSelectionTest {
    private InputSelection.Action<String> action;
    private STTY                          tty;

    @Before
    public void setUp() {
        tty = mock(STTY.class);
        when(tty.getTerminalSize()).thenReturn(new TerminalSize(65, 120));
        when(tty.isInteractive()).thenReturn(true);
    }

    @Test
    public void testEOF() throws IOException {
        try {
            makeSelection(1).select();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: End of input", e.getMessage());
        }
    }

    @Test
    public void testUserInterrupt() throws IOException {
        try {
            makeSelection(1, Char.ESC).select();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: User interrupted: <ESC>", e.getMessage());
        }
    }

    @Test
    public void testSelectFew() throws IOException {
        assertEquals("entry 3", makeSelection(5, '3', '\r').select());
        assertEquals("entry 3", makeSelection(5, Control.DOWN, Control.DOWN, '\r').select());
        assertEquals("entry 3", makeSelection(5, Control.DOWN, Control.DOWN, Control.DOWN, Control.UP, '\r').select());
        assertEquals("entry 4", makeSelection(5, Control.RIGHT, Control.UP, '\r').select());
        assertEquals("entry 2", makeSelection(5, Control.END, Control.LEFT, Control.DOWN, '\r').select());
    }

    @SuppressWarnings("unchecked")
    private InputSelection<String> makeSelection(int num, Object... chars) throws IOException {
        Terminal terminal = TerminalTestUtils.getTerminal(tty, chars);

        this.action = mock(InputSelection.Action.class);

        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (int i = 1; i <= num; ++i) {
            builder.add("entry " + i);
        }

        // There is a bug in oracle JDK 8 that fails this <> generic check.
        return new InputSelection<String>(terminal,
                                    "select",
                                    builder.build(),
                                    ImmutableList.of(new InputSelection.Command<>('a', "Action", action),
                                                     new InputSelection.Command<>('\r', "Select", (s, t) -> InputSelection.Reaction.SELECT, true)),
                                    (s, c) -> s);
    }
}
