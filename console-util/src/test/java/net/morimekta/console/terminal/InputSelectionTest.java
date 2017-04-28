package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.test_utils.ConsoleWatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Testing the line input.
 */
public class InputSelectionTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    private InputSelection.Action<String> action;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        action = mock(InputSelection.Action.class);
    }

    @Test
    public void testEOF() throws IOException {
        try {
            selection(1).select();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: End of input", e.getMessage());
        }
    }

    @Test
    public void testUserInterrupt() throws IOException {
        try {
            selection(1, Char.ESC).select();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertEquals("java.io.IOException: User interrupted: <ESC>", e.getMessage());
        }
    }

    @Test
    public void testSelectFew() throws IOException {
        assertEquals("entry 3", selection(5, '3', '\r').select());
        assertEquals("entry 3", selection(5, Control.DOWN, Control.DOWN, '\n').select());
        assertEquals("entry 3", selection(5, Control.DOWN, Control.DOWN, Control.DOWN, Control.UP, '\r').select());
        assertEquals("entry 4", selection(5, Control.RIGHT, Control.UP, '\r').select());
        assertEquals("entry 2", selection(5, Control.END, Control.LEFT, Control.DOWN, '\r').select());
    }

    @Test
    public void testActions() throws IOException {
        assertThat(selection(150,
                             'x').select(),
                   is(nullValue()));
        assertThat(selection(150,
                             Control.DOWN,
                             Control.DELETE,
                             Control.DPAD_MID,
                             '\t',
                             Char.CR).select("entry 100"),
                   is("entry 50"));
    }

    private InputSelection<String> selection(int num, Object... in) throws IOException {
        if (in.length > 0) {
            console.setInput(in);
        }

        LinkedList<String> builder = new LinkedList<>();
        for (int i = 1; i <= num; ++i) {
            builder.add("entry " + i);
        }

        List<InputSelection.Command<String>> commands =
                ImmutableList.of(new InputSelection.Command<>('a', "Action", action),
                                 new InputSelection.Command<>(Control.DELETE, "Delete", (s, lp) -> {
                                     Collections.reverse(builder);
                                     return InputSelection.Reaction.UPDATE_KEEP_ITEM;
                                 }),
                                 new InputSelection.Command<>(Control.DPAD_MID, "Delete", (s, lp) -> {
                                     Collections.reverse(builder);
                                     return InputSelection.Reaction.UPDATE_KEEP_POSITION;
                                 }),
                                 new InputSelection.Command<>('x', "Delete", (s, lp) -> InputSelection.Reaction.EXIT),
                                 new InputSelection.Command<>(Char.TAB,
                                                              "Delete",
                                                              (s, t) -> InputSelection.Reaction.STAY),
                                 new InputSelection.Command<>('\r',
                                                              "Select",
                                                              (s, t) -> InputSelection.Reaction.SELECT,
                                                              true));

        // There is a bug in oracle JDK 8 that fails this <> generic check.
        return new InputSelection<>(new Terminal(console.tty()),
                                    "select",
                                    builder,
                                    commands,
                                    (s, c) -> s);
    }
}
