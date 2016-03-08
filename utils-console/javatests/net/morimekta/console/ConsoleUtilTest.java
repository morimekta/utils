package net.morimekta.console;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for console util static methods.
 */
public class ConsoleUtilTest {
    @Test
    public void testPrintableWidth() {
        assertEquals(0, ConsoleUtil.printableWidth(""));
        assertEquals(6, ConsoleUtil.printableWidth("\033[32mAbcdef\033[0m"));
    }

    @Test
    public void testExpandTabs() {
        assertEquals("a   b", ConsoleUtil.expandTabs("a\tb"));
    }
}
