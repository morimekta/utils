package net.morimekta.console.terminal;

import com.google.common.collect.ImmutableList;
import net.morimekta.console.chr.Char;
import net.morimekta.console.chr.Control;
import net.morimekta.console.test_utils.ConsoleWatcher;
import net.morimekta.util.Strings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

import static net.morimekta.console.chr.CharUtil.alt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Testing the line input.
 */
public class InputLineTest {
    @Rule
    public ConsoleWatcher console = new ConsoleWatcher();

    private Terminal terminal;

    @Before
    public void setUp() {
        terminal = new Terminal(console.tty());
    }

    @Test
    public void testOutput_simple() throws IOException {
        InputLine li = new InputLine(terminal, "Test");

        assertThat(console.output(), is(""));

        console.setInput('a', Control.LEFT, 'b', Char.CR);

        assertThat(li.readLine(), is("ba"));
        assertThat(console.output(),
                   is("Test: " +
                      "\r\033[KTest: a" +
                      "\r\033[KTest: a\033[1D" +
                      "\r\033[KTest: ba\033[1D"));
    }

    @Test
    public void testEOF() throws IOException {
        try {
            new InputLine(terminal, "test").readLine();
            fail("No exception");
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(),
                       is("java.io.IOException: End of input."));
        }
    }

    @Test
    public void testMovements_1() throws IOException {
        console.setInput("aba",
                         Control.LEFT,
                         Control.LEFT,
                         "cd",
                         Control.CTRL_LEFT,
                         Char.DEL, // BS
                         "e",
                         Control.RIGHT,
                         "f",
                         Control.CTRL_RIGHT,
                         "g",
                         Char.CR);
        assertThat(new InputLine(terminal, "test").readLine(),
                   is("eafcdbag"));
    }

    @Test
    public void testMovements_2() throws IOException {
        console.setInput("aba",
                         Control.HOME,
                         "cd",
                         Control.END,
                         Char.DEL, // BS
                         "g",
                         Char.CR);
        assertThat(new InputLine(terminal, "test").readLine(),
                   is("cdabg"));
    }

    @Test
    public void testMovements_3() throws IOException {
        console.setInput(Control.CTRL_LEFT,
                         alt('w'),
                         Control.HOME,
                         Control.CTRL_RIGHT,
                         alt('d'),
                         Char.CR);
        assertThat(new InputLine(terminal, "test")
                           .readLine("first second third fourth"),
                   is("first fourth"));
    }

    @Test
    public void testMovements_4() throws IOException {
        console.setInput(Control.CTRL_LEFT,
                         Control.CTRL_LEFT,
                         alt('k'),
                         Char.CR);
        assertThat(new InputLine(terminal, "test")
                           .readLine("first second third fourth"),
                   is("first second "));

        console.setInput(Control.CTRL_LEFT,
                         Control.CTRL_LEFT,
                         alt('u'),
                         Char.CR);
        assertThat(new InputLine(terminal, "test")
                           .readLine("first second third fourth"),
                   is("third fourth"));
    }

    @Test
    public void testCharValidator() {
        console.setInput('\t', 'a', '\n');
        assertThat(new InputLine(terminal, "test").readLine(),
                   is("a"));
        assertThat(console.output(),
                   is("test: " +
                      "\r\033[KInvalid character: '\\t'\r\n" +
                      "\r\033[Ktest: " +
                      "\r\033[Ktest: a"));

        // Custom char validator.
        console.reset();
        console.setInput('æ', 'a', '\n');
        assertThat(new InputLine(terminal,
                                 "test",
                                 (c, lp) -> {
                                     if ('a' <= c.asInteger() && c.asInteger() <= 'z')
                                         return true;
                                     lp.println("Char: " + c);
                                     return false;
                                 },
                                 null,
                                 null).readLine(),
                   is("a"));
        assertThat(console.output(),
                   is("\r\ntest: " +
                      "\r\033[KChar: æ\r\n" +
                      "\r\033[Ktest: " +
                      "\r\033[Ktest: a"));
    }

    @Test
    public void testLineValidator() {
        console.setInput("\nb\n");
        assertThat(new InputLine(terminal, "test").readLine(),
                   is("b"));
        assertThat(console.output(),
                   is("test: " +
                      "\r\033[KOutput needs at least 1 character.\r\n" +
                      "\r\033[Ktest: " +
                      "\r\033[Ktest: b"));

        // Custom char validator.
        console.reset();
        console.setInput("a\nb\n");
        assertThat(new InputLine(terminal,
                                 "test",
                                 null,
                                 (l, lp) -> {
                                     if (l.length() > 1)
                                         return true;
                                     lp.println("Line: " + l.length());
                                     return false;
                                 },
                                 null).readLine(),
                   is("ab"));
        assertThat(console.output(),
                   is("\r\ntest: " +
                      "\r\033[Ktest: a" +
                      "\r\033[KLine: 1\r\n" +
                      "\r\033[Ktest: a" +
                      "\r\033[Ktest: ab"));

        // Specified initial value not valid.
        try {
            new InputLine(terminal,
                          "test",
                          null,
                          (l, lp) -> {
                              if (l.length() > 1)
                                  return true;
                              lp.println("Line: " + l.length());
                              return false;
                          },
                          null).readLine("a");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid initial value: a"));
        }
    }

    @Test
    public void testTabCompletion() {
        List<String> vals = ImmutableList.of(
                "editors",
                "edmonds");
        InputLine.TabCompletion completion = (pre, lp) -> {
            List<String> opts = vals.stream()
                                    .filter(s -> s.startsWith(pre))
                                    .collect(Collectors.toList());
            if (opts.size() == 1) {
                return opts.get(0);
            }
            if (opts.size() > 1) {
                lp.println("pre: " + Strings.join(", ", opts));
            }
            return null;
        };

        console.setInput("a\t", Char.BS, "e\tdi\t\n");
        assertThat(new InputLine(terminal,
                                 "test",
                                 null,
                                 null,
                                 completion).readLine(),
                   is("editors"));
        assertThat(console.output(),
                   is("test: " +
                      "\r\033[Ktest: a" +
                      "\r\033[Ktest: " +
                      "\r\033[Ktest: e" +
                      "\r\033[Kpre: editors, edmonds\r\n" +
                      "\r\033[Ktest: e" +
                      "\r\033[Ktest: ed" +
                      "\r\033[Ktest: edi" +
                      "\r\033[Ktest: editors"));
    }

    @Test
    public void testInterrupt() {
        console.setInput("\033");
        try {
            new InputLine(terminal,
                          "test").readLine();
            fail();
        } catch (UncheckedIOException e) {
            assertThat(e.getMessage(),
                       is("java.io.IOException: User interrupted: <ESC>"));
        }
    }
}
