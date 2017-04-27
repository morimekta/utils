package net.morimekta.console.chr;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Created by morimekta on 22.04.17.
 */
public class ControlTest {
    @Test
    public void testRemapping() {
        assertThat(new Control("\033OH"), is(Control.HOME));
        assertThat(new Control("\033[H"), is(Control.HOME));
    }

    @Test
    public void testAsString() {
        assertThat(Control.UP.asString(), is("<up>"));
        assertThat(Control.DOWN.asString(), is("<down>"));
        assertThat(Control.RIGHT.asString(), is("<right>"));
        assertThat(Control.LEFT.asString(), is("<left>"));
        assertThat(Control.CTRL_UP.asString(), is("<C-up>"));
        assertThat(Control.CTRL_DOWN.asString(), is("<C-down>"));
        assertThat(Control.CTRL_RIGHT.asString(), is("<C-right>"));
        assertThat(Control.CTRL_LEFT.asString(), is("<C-left>"));
        assertThat(Control.CURSOR_ERASE.asString(), is("<cursor-erase>"));
        assertThat(Control.CURSOR_SAVE.asString(), is("<cursor-save>"));
        assertThat(Control.CURSOR_RESTORE.asString(), is("<cursor-restore>"));
        assertThat(Control.DPAD_MID.asString(), is("<dpa-mid>"));
        assertThat(Control.INSERT.asString(), is("<insert>"));
        assertThat(Control.DELETE.asString(), is("<delete>"));
        assertThat(Control.HOME.asString(), is("<home>"));
        assertThat(Control.END.asString(), is("<end>"));
        assertThat(Control.PAGE_UP.asString(), is("<pg-up>"));
        assertThat(Control.PAGE_DOWN.asString(), is("<pg-down>"));
        assertThat(Control.F1.asString(), is("<F1>"));
        assertThat(Control.F2.asString(), is("<F2>"));
        assertThat(Control.F3.asString(), is("<F3>"));
        assertThat(Control.F4.asString(), is("<F4>"));
        assertThat(Control.F5.asString(), is("<F5>"));
        assertThat(Control.F6.asString(), is("<F6>"));
        assertThat(Control.F7.asString(), is("<F7>"));
        assertThat(Control.F8.asString(), is("<F8>"));
        assertThat(Control.F9.asString(), is("<F9>"));
        assertThat(Control.F10.asString(), is("<F10>"));
        assertThat(Control.F11.asString(), is("<F11>"));
        assertThat(Control.F12.asString(), is("<F12>"));
        assertThat(new Control("\033f").asString(), is("<alt-f>"));
        assertThat(new Control("\033C").asString(), is("<alt-shift-c>"));
        assertThat(new Control("\033[G").asString(), is("\\033[G"));
    }

    @Test
    public void testProperties() {
        assertThat(Control.F1.length(), is(3));
        assertThat(Control.F1.hashCode(), is(not(Control.CTRL_DOWN.hashCode())));
        assertThat(Control.F1.compareTo(Control.CTRL_DOWN), is(-12));
        assertThat(Control.F1.compareTo(Unicode.NBSP), is(-1));
        assertThat(Control.F1.equals(Control.F1), is(true));
        assertThat(Control.F1.equals(null), is(false));
        assertThat(Control.F1.equals(new Object()), is(false));

    }
}
