package net.morimekta.console.args;

import net.morimekta.console.util.TerminalSize;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * TODO(steineldar): Make a proper class description.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(TerminalSize.class)
public class ArgumentOptionsTest {
    @Before
    public void setUp() {
        mockStatic(TerminalSize.class);
        when(TerminalSize.get()).thenReturn(new TerminalSize(65, 120));
        when(TerminalSize.isInteractive()).thenReturn(true);
    }

    @Test
    public void testDefaultsShown() {
        assertTrue(ArgumentOptions.defaults()
                                  .getDefaultsShown());
        assertTrue(ArgumentOptions.defaults()
                                  .withDefaultsShown(true)
                                  .getDefaultsShown());
        assertFalse(ArgumentOptions.defaults()
                                   .withDefaultsShown(false)
                                   .getDefaultsShown());
    }

    @Test
    public void testUsageWidth() {
        assertEquals(80, ArgumentOptions.defaults()
                                        .getUsageWidth());
        assertEquals(100, ArgumentOptions.defaults()
                                         .withUsageWidth(100)
                                         .getUsageWidth());
        assertEquals(100, ArgumentOptions.defaults()
                                         .withMaxUsageWidth(100)
                                         .getUsageWidth());

        assertEquals(120, ArgumentOptions.defaults()
                                         .withMaxUsageWidth(144)
                                         .getUsageWidth());
        assertEquals(144, ArgumentOptions.defaults()
                                         .withUsageWidth(144)
                                         .getUsageWidth());


        when(TerminalSize.get()).thenThrow(new UncheckedIOException(new IOException("Oops")));
        when(TerminalSize.isInteractive()).thenReturn(false);

        assertEquals(144, ArgumentOptions.defaults()
                                         .withMaxUsageWidth(144)
                                         .getUsageWidth());
        assertEquals(144, ArgumentOptions.defaults()
                                         .withUsageWidth(144)
                                         .getUsageWidth());
    }

    @Test
    public void testOptionComparator() {
        assertNull(ArgumentOptions.defaults().getOptionComparator());
        Comparator<BaseOption> comp = (bo1, bo2) -> bo1.getName().compareTo(bo2.getName());

        assertSame(comp, ArgumentOptions.defaults()
                                        .withOptionComparator(comp)
                                        .getOptionComparator());
    }
}
