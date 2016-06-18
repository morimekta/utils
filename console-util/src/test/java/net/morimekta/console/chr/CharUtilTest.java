package net.morimekta.console.chr;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for console util static methods.
 */
public class CharUtilTest {
    @Test
    public void testPrintableWidth() {
        assertEquals(0, CharUtil.printableWidth(""));
        assertEquals(6, CharUtil.printableWidth("\033[32mAbcdef\033[0m"));
    }

    @Test
    public void testStripNonPrintable() {
        assertEquals("Abcdef", CharUtil.stripNonPrintable("\033[32mAbcdef\033[0m"));
    }

    @Test
    public void testExpandTabs() {
        assertEquals("a   b", CharUtil.expandTabs("a\tb"));
    }

    @Test
    public void testConstructor()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor constructor = CharUtil.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance();
        constructor.setAccessible(false);
    }
}
