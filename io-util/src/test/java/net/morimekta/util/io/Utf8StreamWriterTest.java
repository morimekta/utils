package net.morimekta.util.io;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Utf8StreamWriterTest {
    @Test
    public void testFlushClose() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Utf8StreamWriter writer = new Utf8StreamWriter(out);

        writer.write("輸ü$Ѹ~");

        assertThat(out.toByteArray(), is(new byte[]{
                -16, -81, -89, -97, -61, -68, 36, -47, -72, 126
        }));

        writer.flush();
        writer.flush();
        writer.close();
        writer.close();

        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Utf8StreamReader reader = new Utf8StreamReader(in);
        assertThat(IOUtils.readString(reader), is("輸ü$Ѹ~"));
    }
}
