package net.morimekta.console.chr;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static net.morimekta.console.chr.CharUtil.center;
import static net.morimekta.console.chr.CharUtil.inputBytes;
import static net.morimekta.console.chr.CharUtil.leftJust;
import static net.morimekta.console.chr.CharUtil.leftPad;
import static net.morimekta.console.chr.CharUtil.makeBorder;
import static net.morimekta.console.chr.CharUtil.makeNumeric;
import static net.morimekta.console.chr.CharUtil.rightJust;
import static net.morimekta.console.chr.CharUtil.rightPad;
import static net.morimekta.console.chr.CharUtil.stripNonPrintable;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        assertEquals("Abcdef", stripNonPrintable("\033[32mAbcdef\033[0m"));
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 512; ++i) {
            if (i == '\033') {
                b.append((char) i);
            }
            b.append(new Unicode(i).toString());
        }
        assertThat(stripNonPrintable(b.toString()).length(), is(448));
    }

    @Test
    public void testExpandTabs() {
        assertEquals("a   b", CharUtil.expandTabs("a\tb"));
    }

    @Test
    public void testClipWidth() {
        assertThat(CharUtil.clipWidth("b𪚲𪚲𪚲𪚲𪚲𪚲c", 10),
                   is("b𪚲𪚲𪚲𪚲"));
        assertThat(CharUtil.clipWidth(Color.GREEN + "b𪚲𪚲𪚲𪚲𪚲𪚲c" + Color.CLEAR, 10),
                   is(Color.GREEN + "b𪚲𪚲𪚲𪚲" + Color.CLEAR));
    }

    @Test
    public void testJust() {
        assertThat(rightPad("\033[02m𪚲𪚲\033[00m", 10),
                   is("\033[02m𪚲𪚲\033[00m      "));
        assertThat(leftJust("\033[02m𪚲𪚲\033[00m", 10),
                   is("\033[02m𪚲𪚲\033[00m      "));
        assertThat(leftPad("\033[02m𪚲𪚲\033[00m", 10),
                   is("      \033[02m𪚲𪚲\033[00m"));
        assertThat(rightJust("\033[02m𪚲𪚲\033[00m", 10),
                   is("      \033[02m𪚲𪚲\033[00m"));
        assertThat(center("\033[02m𪚲𪚲\033[00m", 10),
                   is("   \033[02m𪚲𪚲\033[00m   "));

        assertThat(leftPad("\033[02m𪚲𪚲\033[00m", 3),  is("\033[02m𪚲𪚲\033[00m"));
        assertThat(rightPad("\033[02m𪚲𪚲\033[00m", 3), is("\033[02m𪚲𪚲\033[00m"));
        assertThat(center("\033[02m𪚲𪚲\033[00m", 3),   is("\033[02m𪚲𪚲\033[00m"));
    }

    @Test
    public void testBorders() {
        int ok = 0;
        int bad = 0;

        for (int[] a : new int[][]{
                {0, 0, 0, 1},
                {0, 0, 1, 1},
                {0, 1, 0, 1},
                {0, 1, 1, 1},
                {1, 1, 1, 1},

                {0, 0, 0, 2},
                {0, 0, 2, 2},
                {0, 2, 0, 2},
                {0, 2, 2, 2},
                {2, 2, 2, 2},

                {0, 1, 0, 2},
                {0, 0, 1, 2},
                {0, 0, 2, 1},

                {0, 1, 1, 2},
                {0, 1, 2, 1},
                {0, 2, 1, 1},
                {2, 1, 1, 1},

                {0, 2, 2, 1},
                {0, 2, 1, 2},
                {0, 1, 2, 2},
                {1, 2, 2, 2},

                {1, 1, 2, 2},
                {1, 2, 1, 2},

                // dl - single (no stumps)
                {0, 0, 3, 3},
                {0, 3, 0, 3},
                {0, 3, 3, 3},
                {3, 3, 3, 3},

                {0, 0, 1, 3},
                {0, 0, 3, 1},
                {0, 3, 1, 3},
                {0, 1, 3, 1},
                {1, 3, 1, 3},
                {3, 3, 3, 3},

                // Bad.
                {1, 1, 1, 3},
                {-1, 0, 0, 0},
                {4, 0, 0, 0},
        }) {
            for (int i = 0; i < 4; ++i) {
                try {
                    makeBorder(a[pos(i, 0)], a[pos(i, 1)], a[pos(i, 2)], a[pos(i, 3)]);
                    ++ok;
                } catch (IllegalArgumentException e) {
                    // e.printStackTrace();
                    ++bad;
                }
            }
        }

        assertThat(ok, is(132));
        assertThat(bad, is(12));
    }

    @Test
    public void testNumeric() {
        try {
            makeNumeric(0);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("No circled numeric for 0"));
        }
        for (int i = 1; i <= 50; ++i) {
            assertThat(makeNumeric(i), is(instanceOf(Unicode.class)));
        }
        try {
            makeNumeric(51);
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("No circled numeric for 51"));
        }
    }

    int pos(int i, int off) {
        return (i + off) % 4;
    }

    @Test
    public void testInputBytes() {
        assertThat(inputBytes(127, ' ', Control.HOME, new Unicode(Char.ESC)),
                   is(bytes(127, 32, 27, 91, 49, 126, 27, 27)));
        assertThat(inputBytes(27),
                   is(bytes(27, 27)));
        assertThat(inputBytes('\033'),
                   is(bytes(27, 27)));
        assertThat(inputBytes("boo"),
                   is(bytes(98, 111, 111)));
    }

    byte[] bytes(int... b) {
        byte[] a = new byte[b.length];
        for (int i = 0; i < b.length; ++i) {
            a[i] = (byte) b[i];
        }
        return a;
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
