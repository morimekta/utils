package net.morimekta.util.io;

import com.google.common.collect.ImmutableList;
import net.morimekta.util.Strings;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class LineBufferedReaderTest {
    private Reader reader;

    @Before
    public void setUp() {
        ByteArrayInputStream in = new ByteArrayInputStream(
                ("first line\n" +
                 "second line\n" +
                 "third line").getBytes(StandardCharsets.UTF_8));
        reader = new Utf8StreamReader(in);
    }

    private void testTheBasics(LineBufferedReader sut) throws IOException {
        // not started reading, there is no "current" line...
        assertThat(sut.getLine(), is(""));
        // so it cannot have anything remaining either.
        assertThat(sut.getRestOfLine(), is(""));
        assertThat(sut.read(new char[2]), is(2));
        assertThat(sut.getLine(), is("first line"));
        assertThat(IOUtils.readString(sut, " "), is("rst"));

        assertThat(sut.getLine(), is("first line"));
        assertThat(sut.getLineNo(), is(1));
        assertThat(sut.getLinePos(), is(6));

        assertThat(sut.getRestOfLine(), is("line"));
        assertThat(sut.getLine(), is("first line"));

        assertThat(sut.getLineNo(), is(1));
        assertThat(sut.getLinePos(), is(11));

        assertThat(sut.getRemainingLines(false), is(ImmutableList.of(
                "second line",
                "third line")));

        assertThat(sut.getLineNo(), is(3));
        assertThat(sut.getLinePos(), is(10));

        sut.close();
    }

    @Test
    public void testDefaultReader() throws IOException {
        testTheBasics(new LineBufferedReader(reader));
    }

    @Test
    public void testPreLoadedReader() throws IOException {
        testTheBasics(new LineBufferedReader(reader, 1 << 10, true));
    }

    @Test
    public void testLongLines() throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(
                Strings.times(Strings.times(IOUtilsTest.lorem + "\t\t", 2) + "\n", 2).getBytes(StandardCharsets.UTF_8));
        reader = new Utf8StreamReader(in);
        LineBufferedReader sut = new LineBufferedReader(reader, IOUtilsTest.lorem.length() - 10);

        assertThat(IOUtils.readString(sut, "\t\t"), is(IOUtilsTest.lorem));
        assertThat(sut.getRestOfLine().trim(), is(IOUtilsTest.lorem));
        assertThat(sut.getRemainingLines(true), is(ImmutableList.of(
                IOUtilsTest.lorem + "\t\t" + IOUtilsTest.lorem)));
    }
}
