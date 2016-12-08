package net.morimekta.console.args;

import net.morimekta.console.util.STTY;
import net.morimekta.console.util.TerminalSize;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * TODO(steineldar): Make a proper class description.
 */
public class ArgumentOptionsTest {
    private STTY tty;

    @Before
    public void setUp() {
        tty = mock(STTY.class);
        when(tty.getTerminalSize()).thenReturn(new TerminalSize(65, 120));
        when(tty.isInteractive()).thenReturn(true);
    }

    @Test
    public void testDefaultsShown() {
        assertTrue(ArgumentOptions.defaults(tty)
                                  .getDefaultsShown());
        assertTrue(ArgumentOptions.defaults(tty)
                                  .withDefaultsShown(true)
                                  .getDefaultsShown());
        assertFalse(ArgumentOptions.defaults()
                                   .withDefaultsShown(false)
                                   .getDefaultsShown());
    }

    @Test
    public void testUsageWidth() {
        assertEquals(80, ArgumentOptions.defaults(tty)
                                        .getUsageWidth());
        assertEquals(100, ArgumentOptions.defaults(tty)
                                         .withUsageWidth(100)
                                         .getUsageWidth());
        assertEquals(100, ArgumentOptions.defaults(tty)
                                         .withMaxUsageWidth(100)
                                         .getUsageWidth());

        assertEquals(120, ArgumentOptions.defaults(tty)
                                         .withMaxUsageWidth(144)
                                         .getUsageWidth());
        assertEquals(144, ArgumentOptions.defaults(tty)
                                         .withUsageWidth(144)
                                         .getUsageWidth());

        reset(tty);
        when(tty.getTerminalSize()).thenThrow(new UncheckedIOException(new IOException("Oops")));
        when(tty.isInteractive()).thenReturn(false);

        assertEquals(144, ArgumentOptions.defaults(tty)
                                         .withMaxUsageWidth(144)
                                         .getUsageWidth());
        assertEquals(144, ArgumentOptions.defaults(tty)
                                         .withUsageWidth(144)
                                         .getUsageWidth());
    }

    @Test
    public void testOptionComparator() {
        assertNull(ArgumentOptions.defaults(tty).getOptionComparator());
        Comparator<BaseOption> comp = (bo1, bo2) -> bo1.getName().compareTo(bo2.getName());

        assertSame(comp, ArgumentOptions.defaults(tty)
                                        .withOptionComparator(comp)
                                        .getOptionComparator());
    }
}
