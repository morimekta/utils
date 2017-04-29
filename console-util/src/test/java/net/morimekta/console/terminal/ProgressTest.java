package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.console.test_utils.FakeClock;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedList;

import static net.morimekta.console.chr.CharUtil.stripNonPrintable;
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

        assertThat(stripNonPrintable(console.output()),
                   is("\r" +
                      "Title: [/---------------------------------------------------------------------------------------------------]   0%\r" +
                      "Title: [#######################################-------------------------------------------------------------]  39%\r" +
                      "Title: [###################################################################################################\\]  99%\r" +
                      "Title: [####################################################################################################] 100% @   0.9 s  "));
    }

    @Test
    public void testLinePrinter() {
        LinkedList<String> lines = new LinkedList<>();
        Progress p = new Progress(l -> lines.add(stripNonPrintable(l)), Progress.Spinner.CLOCK, "Foo", 100);
        p.update(37);

        assertThat(lines, is(ImmutableList.of(
                "Foo: [🕑⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅]   0%",
                "Foo: [#####################################🕒⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅]  37%")));
    }

    @Test
    public void testTerminal() {
        Progress p = new Progress(new Terminal(console.tty()), Progress.Spinner.ARROWS, "Foo", 100);
        p.update(37);

        assertThat(console.output(), is(
                "\r\033[K" +
                "Foo: [\033[32m\033[1;33m⭦\033[0m\033[33m⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅\033[0m]   0%" +
                "\r\033[K" +
                "Foo: [\033[32m#####################################\033[1;33m⭡\033[0m\033[33m⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅\033[0m]  37%"));
    }

    @Test
    public void testRemainingDuration() {
        FakeClock clock = new FakeClock();
        LinkedList<String> lines = new LinkedList<>();

        Progress p = new Progress(null,
                                  l -> lines.add(stripNonPrintable(l)),
                                  clock,
                                  Progress.Spinner.BLOCKS,
                                  "Title",
                                  127);

        clock.tick(2000);
        p.update(50);
        clock.tick(1500);
        p.update(100);
        clock.tick(1321);
        p.update(127);

        assertThat(lines, is(ImmutableList.of(
                "Title: [▂⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅]   0%",
                "Title: [#######################################▃⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅]  39%",
                "Title: [##############################################################################▄⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅⋅]  78% +(  0.9 s  )",
                "Title: [####################################################################################################] 100% @   4.8 s  ")));
    }
}
