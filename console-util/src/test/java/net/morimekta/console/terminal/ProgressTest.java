package net.morimekta.console.terminal;

import net.morimekta.console.chr.CharUtil;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.test_utils.FakeClock;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProgressTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    @Test
    public void testProgress() {
        FakeClock clock = new FakeClock();

        Progress p = new Progress(new Terminal(console.tty()),
                                  null,
                                  clock,
                                  null,
                                  "Title",
                                  127);
        clock.tick(200);
        p.update(50);
        clock.tick(300);
        p.update(126);
        clock.tick(400);
        p.update(127);

        assertThat(CharUtil.stripNonPrintable(console.output()),
                   is("\r" +
                      "Title: [/---------------------------------------------------------------------------------------------------]   0%\r" +
                      "Title: [#######################################-------------------------------------------------------------]  39%\r" +
                      "Title: [###################################################################################################\\]  99%\r" +
                      "Title: [####################################################################################################] 100% @   0.9 s  "));
    }
}
