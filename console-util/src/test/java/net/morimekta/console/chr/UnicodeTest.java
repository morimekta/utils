package net.morimekta.console.chr;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Test for unicode char.
 */
public class UnicodeTest {
    @Test
    public void testLength() {
        assertThat(new Unicode('優').length(), is(1));
        assertThat(new Unicode(0x2A6B2).length(), is(2));
        assertThat(new Unicode('a').length(), is(1));
        // TAB is rather 'undefined', but put in the bucket with other
        // control chars.
        assertThat(new Unicode(Char.TAB).length(), is(1));
        assertThat(Unicode.NBSP.length(), is(1));
    }

    @Test
    public void testPrintableWidth() {
        assertThat(Unicode.NBSP.printableWidth(), is(1));
        assertThat(Unicode.ALM.printableWidth(), is(0));
        assertThat(new Unicode(0x2A6B2).printableWidth(), is(2));
        assertThat(new Unicode('優').printableWidth(), is(2));
        assertThat(new Unicode('a').printableWidth(), is(1));
        // TAB is rather 'undefined', but put in the bucket with other
        // control chars.
        assertThat(new Unicode(Char.TAB).printableWidth(), is(0));
    }

    @Test
    public void testToString() {
        assertThat(Unicode.NBSP.toString(), is("\u00A0"));
        assertThat(new Unicode(0x2A6B2).toString(), is("𪚲"));
        assertThat(new Unicode('優').toString(), is("優"));
        assertThat(new Unicode('a').toString(), is("a"));
        // TAB is rather 'undefined', but put in the bucket with other
        // control chars.
        assertThat(new Unicode(Char.TAB).toString(), is("\t"));
    }

    @Test
    public void testAsString() {
        assertAsString(new Unicode(Char.NUL), "<NUL>");
        assertAsString(new Unicode(Char.ABR), "<ABR>");
        assertAsString(new Unicode(Char.EOF), "<EOF>");
        assertAsString(new Unicode(Char.BEL), "<BEL>");
        assertAsString(new Unicode(Char.BS), "<BS>");
        assertAsString(new Unicode(Char.VT), "<VT>");
        assertAsString(new Unicode(Char.ESC), "<ESC>");
        assertAsString(new Unicode(Char.FS), "<FS>");
        assertAsString(new Unicode(Char.GS), "<GS>");
        assertAsString(new Unicode(Char.RS), "<RS>");
        assertAsString(new Unicode(Char.US), "<US>");
        assertAsString(new Unicode(Char.DEL), "<DEL>");

        assertAsString(new Unicode(Char.TAB), "'\\t'");
        assertAsString(new Unicode(Char.LF), "'\\n'");
        assertAsString(new Unicode(Char.FF), "'\\f'");
        assertAsString(new Unicode(Char.CR), "'\\r'");
        assertAsString(new Unicode('\"'), "'\\\"'");
        assertAsString(new Unicode('\''), "'\\\''");
        assertAsString(new Unicode('\\'), "'\\\\'");
        assertAsString(Unicode.NBSP, "<nbsp>");

        assertAsString(new Unicode(15), "'\\017'");
        assertAsString(new Unicode(8192), "'\\u2000'");
        assertAsString(new Unicode(81928192), "'\\u1048\\udc00'");

        assertAsString(new Unicode(0x2A6B2), "'𪚲'");
        assertAsString(new Unicode('優'), "'優'");
    }

    private void assertAsString(Unicode u, String expected) {
        assertThat(u.asString(), is(expected));
    }

    @Test
    public void testEquals() {
        Unicode a = new Unicode('a');
        Unicode a1 = new Unicode('a');
        Unicode b = new Unicode('b');

        assertThat(a.equals(a1), is(true));
        assertThat(a.equals(a), is(true));
        assertThat(a.equals(b), is(false));
        assertThat(a.equals(null), is(false));
        assertThat(a.equals(new Object()), is(false));
    }

    @Test
    public void testCompareTo() {
        Unicode a = new Unicode('a');
        Unicode a1 = new Unicode('a');
        Unicode b = new Unicode('b');

        assertThat(a.compareTo(a1), is(0));
        assertThat(a.compareTo(b), is(-1));
    }
}
