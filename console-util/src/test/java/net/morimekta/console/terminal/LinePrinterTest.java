package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.LinkedList;

import static net.morimekta.console.chr.CharUtil.stripNonPrintable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LinePrinterTest {
    @Test
    public void testSimpleLines() {
        LinkedList<String> lines   = new LinkedList<>();
        LinePrinter        printer = l -> lines.add(stripNonPrintable(l));

        printer.println("simple");
        printer.info("the info 45%");
        printer.warn("the warn");
        printer.error("the err");
        printer.fatal("the fat");

        assertThat(lines, is(ImmutableList.of(
                "simple",
                "[info] the info 45%",
                "[warn] the warn",
                "[error] the err",
                "[FATAL] the fat")));
    }

    @Test
    public void testFormattedLines() {
        LinkedList<String> lines   = new LinkedList<>();
        LinePrinter        printer = l -> lines.add(stripNonPrintable(l));

        printer.info("the %d info", 1);
        printer.warn("the %d warn", 2);
        printer.error("the %d err", 3);
        printer.fatal("the %d fat", 4);
        printer.formatln("the %d format", 5);

        assertThat(lines, is(ImmutableList.of(
                "[info] the 1 info",
                "[warn] the 2 warn",
                "[error] the 3 err",
                "[FATAL] the 4 fat",
                "the 5 format")));
    }
}
