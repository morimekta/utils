package net.morimekta.console.util;

import net.morimekta.console.util.ConsoleUtil;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = ConsoleUtil.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }
}
