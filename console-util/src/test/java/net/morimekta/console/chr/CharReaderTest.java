package net.morimekta.console.chr;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CharReaderTest {
    private static InputStream origIn;

    @BeforeClass
    public static void setUpClass() {
        origIn = System.in;
    }

    @After
    public void tearDown() {
        System.setIn(origIn);
    }

    @Test
    public void testEscEnd() throws IOException {
        Reader in = mock(Reader.class);
        when(in.read()).thenReturn((int) '\033')
                       .thenReturn(-1);
        when(in.ready()).thenReturn(true);

        CharReader reader = new CharReader(in);

        assertThat(reader.read(), is(new Unicode('\033')));
    }

    @Test
    public void testReader() throws IOException {
        assertInput("");
        assertInput("a", 'a');
        assertInput("\033", '\033');
        assertInput("\033\033", '\033');
        assertInput(Color.RED.toString(), Color.RED);
        assertInput(Control.CTRL_DOWN.toString(), Control.CTRL_DOWN);

        assertInput(format("%sa%s%s", Color.RED, Control.CTRL_DOWN, CharUtil.makeNumeric(33)),
                    Color.RED, "a", Control.CTRL_DOWN, CharUtil.makeNumeric(33));
        assertInput(format("%s", Control.cursorSetPos(33)),
                    Control.cursorSetPos(33));

        assertInput("\033OB", new Control("\033OB"));
        assertInput("\033a", new Control("\033a"));
        assertInput("𪚲", 0x2A6B2);
    }

    @Test
    public void testReadFailures() {
        assertFailure("\033O", "Unexpected end of stream.");
        assertFailure("\033[", "Unexpected end of stream.");
        assertFailure("\033[5", "Unexpected end of stream.");
        assertFailure("\033[mb", "Invalid color control sequence: \"\\033[m\"");
        assertFailure("\033[.b", "Invalid escape sequence: \"\\033[.\"");
        assertFailure("\033O5b", "Invalid escape sequence: \"\\033O5\"");
        assertFailure("\0335b", "Invalid escape sequence: \"\\0335\"");
    }

    private void assertInput(String in, Object... out) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                in.getBytes(UTF_8));
        System.setIn(bais);

        CharReader reader = new CharReader();

        List<Char> expected = CharUtil.inputChars(out);
        List<Char> actual = new LinkedList<>();
        Char c;
        while ((c = reader.read()) != null) {
            actual.add(c);
        }

        assertThat(actual, is(expected));
    }

    private void assertFailure(String in, String message) {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                in.getBytes(UTF_8));
        System.setIn(bais);

        CharReader reader = new CharReader();

        try {
            while (reader.read() != null);
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is(message));
        }
    }
}
