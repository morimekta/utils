package net.morimekta.util.io;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LineBufferedReaderTest {
    private Reader in;
    private Reader lin;

    @Before
    public void setUp() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                ("program_name = \"number\"\n" +
                 "namespaces = {\n" +
                 "  \"java\": \"net.morimekta.test.number\"\n" +
                 "}\n" +
                 "decl = [\n" +
                 "  {\n" +
                 "    decl_typedef = {\n" +
                 "      type = \"double\"\n" +
                 "      name = \"real\"\n" +
                 "    }\n" +
                 "  }\n" +
                 "]").getBytes(StandardCharsets.UTF_8));
        in = new Utf8StreamReader(bais);
        CharArrayWriter writer = new CharArrayWriter();
        for (int i = 0; i < 2; ++i) {
            writer.write(IOUtilsTest.lorem);
            writer.write("\t");
            writer.write(IOUtilsTest.lorem);
            writer.write("\n");
        }
        writer.write(IOUtilsTest.lorem);
        writer.write("\t");
        writer.write(IOUtilsTest.lorem);
        lin = new CharArrayReader(writer.toCharArray());
    }

    private void testTheBasics(LineBufferedReader reader) throws IOException {
        // -----------------
        // --   DEFAULT   --
        // -----------------

        // starting with reader behaviour.

        // not started reading, there is no "current" line...
        assertThat(reader.getLine(), is(""));
        // so it cannot have anything remaining either.
        assertThat(reader.getRestOfLine(), is(""));

        char[] tmp = new char[12];
        assertThat(reader.read(tmp), is(12));
        assertThat(new String(tmp), is("program_name"));

        assertThat(reader.lastChar, is(0));
        assertThat(reader.getLine(), is("program_name = \"number\""));
        assertThat(reader.getLineNo(), is(1));
        assertThat(reader.getLinePos(), is(12));

        assertThat(IOUtils.readString(reader, "{"),
                   is(" = \"number\"\nnamespaces = "));
        assertThat((char) reader.read(), is('\n'));

        assertThat(reader.lastChar, is(0));
        assertThat(reader.getLine(), is("namespaces = {"));
        assertThat(reader.getLineNo(), is(2));
        assertThat(reader.getLinePos(), is(15));  // the position of the newline...

        assertThat(reader.readNextChar(), is(true));
        assertThat((char) reader.lastChar, is(' '));
        assertThat(reader.getLine(), is("  \"java\": \"net.morimekta.test.number\""));
        assertThat(reader.getLineNo(), is(3));
        assertThat(reader.getLinePos(), is(1));
        // The last char (first space) is not consumed, therefore included in the
        // restOfLine.
        assertThat(reader.getRestOfLine(), is("  \"java\": \"net.morimekta.test.number\""));

        assertThat((char) reader.read(), is('}'));
        assertThat(reader.readNextChar(), is(true));
        assertThat((char) reader.lastChar, is('\n'));
        assertThat(reader.readNextChar(), is(true));
        assertThat((char) reader.lastChar, is('d'));
        assertThat(reader.readNextChar(), is(true));
        assertThat((char) reader.lastChar, is('e'));
        assertThat(reader.readNextChar(), is(true));
        assertThat((char) reader.lastChar, is('c'));
        reader.lastChar = 0;

        // not consumed,
        assertThat(reader.getRestOfLine(), is("l = ["));
        assertThat(reader.getRemainingLines(true),
                   is(ImmutableList.of(
                           "{",
                           "decl_typedef = {",
                           "type = \"double\"",
                           "name = \"real\"",
                           "}",
                           "}",
                           "]")));
        assertThat(reader.read(), is(-1));

        reader.close();
    }

    private void testLongLines(LineBufferedReader reader) throws IOException {
        // ------------------
        // --  LONG LINES  --
        // ------------------
        assertThat(IOUtils.readString(reader, '\t'),
                   is(IOUtilsTest.lorem));
        assertThat(IOUtils.readString(reader, '\n'),
                   is(IOUtilsTest.lorem));
        assertThat(IOUtils.readString(reader, '\t'),
                   is(IOUtilsTest.lorem));
        assertThat(IOUtils.readString(reader, '\n'),
                   is(IOUtilsTest.lorem));

        assertThat(IOUtils.readString(reader, ' '), is("Lorem"));
        assertThat(IOUtils.skipUntil(reader, '\t'), is(true));
        assertThat(IOUtils.readString(reader, ' '), is("Lorem"));
        assertThat(IOUtils.readString(reader, ' '), is("ipsum"));
        assertThat(IOUtils.readString(reader, ' '), is("dolor"));
        assertThat(IOUtils.readString(reader, ' '), is("sit"));
        assertThat(IOUtils.readString(reader, ' '), is("amet,"));

        reader.readNextChar();
        char[] tmp = new char[12];
        tmp[0] = '-';
        tmp[11] = '-';
        assertThat(reader.read(tmp, 1, 10), is(10));
        assertThat(new String(tmp), is("-consectetu-"));

        assertThat(reader.getRestOfLine(),
                   is("r adipiscing elit, sed do eiusmod tempor incididunt ut " +
                      "labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud " +
                      "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                      "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum " +
                      "dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non " +
                      "proident, sunt in culpa qui officia deserunt mollit anim id est laborum."));
        assertThat(reader.read(), is(-1));
    }

    @Test
    public void testDefaultReader() throws IOException {
        testTheBasics(new LineBufferedReader(in));
        testLongLines(new LineBufferedReader(lin, 1 << 7));
    }

    @Test
    public void testPreLoadedReader() throws IOException {
        testTheBasics(new LineBufferedReader(in, true));
        testLongLines(new LineBufferedReader(lin, 1 << 7, true));
    }

    @Test
    public void testBadParams() throws IOException {
        LineBufferedReader sut = new LineBufferedReader(in);

        char tmp[] = new char[12];
        try {
            assertThat(sut.read(tmp, -1, 2), is(-1));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("off: -1 len: 2"));
        }

        try {
            assertThat(sut.read(tmp, 7, 8), is(-1));
            fail("no exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("off: 7 len: 8 > char[12]"));
        }

        Reader failing = mock(Reader.class);
        when(failing.read(any(char[].class))).thenThrow(new IOException("fail!"));
        try {
            new LineBufferedReader(failing, true);
            fail("no exception");
        } catch (UncheckedIOException ue) {
            assertThat(ue.getCause().getMessage(), is("fail!"));
        }
    }
}
