package android.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Base64StreamTest {
    @Test
    public void testBase64Stream_fails() throws IOException {
        try {
            new Base64InputStream(null, Base64.CRLF);
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("null input stream"));
        }

        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{
                'A', 'B', '-'});
        Base64InputStream bis = new Base64InputStream(in, Base64.DEFAULT);
        try {
            assertThat(bis.read(), is(0));
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Invalid base64 character '-'"));
        }

        bis.close();
        try {
            bis.read();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Reading from a closed Base64InputStream"));
        }

        try {
            bis.skip(3);
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("skip/reset not supported"));
        }
        try {
            bis.reset();
            fail("no exception");
        } catch (IOException e) {
            assertThat(e.getMessage(), is("mark/reset not supported"));
        }

        InputStream min = mock(InputStream.class);
        bis = new Base64InputStream(min, Base64.DEFAULT);
        when(min.read()).thenThrow(new IOException("e"));
        try {
            bis.read();
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("e"));
        }
    }

    @Test
    public void testInputStream() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(
                "6GccB6".getBytes(UTF_8));
        Base64InputStream in = new Base64InputStream(bais, Base64.URL_SAFE);

        assertThat(in.markSupported(), is(false));
        in.mark(0);  // ignored.

        assertThat(in.read(), is(232));
        assertThat(in.available(), is(3));
        assertThat(in.read(), is(103));
        assertThat(in.available(), is(2));
        assertThat(in.read(), is(28));
        assertThat(in.available(), is(1));
        assertThat(in.read(), is(7));
        assertThat(in.available(), is(0));
        assertThat(in.read(), is(-1));


        bais = new ByteArrayInputStream(
                "6Gcc".getBytes(UTF_8));
        in = new Base64InputStream(bais, Base64.URL_SAFE);

        assertThat(in.markSupported(), is(false));
        in.mark(0);  // ignored.

        assertThat(in.read(), is(232));
        assertThat(in.available(), is(2));
        assertThat(in.read(), is(103));
        assertThat(in.available(), is(1));
        assertThat(in.read(), is(28));
        assertThat(in.available(), is(0));
        assertThat(in.read(), is(-1));
    }

    @Test
    public void testOutputStream_flush() throws IOException {
        OutputStream       baos = mock(OutputStream.class);
        Base64OutputStream out  = new Base64OutputStream(baos, Base64.NO_CLOSE);
        out.flush();
        verify(baos).flush();
        out.close();
        reset(baos);

        try {
            out.flush();
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Flushing a closed Base64OutputStream"));
        }
        try {
            out.write(33);
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("Writing to closed Base64OutputStream"));
        }

        baos = mock(OutputStream.class);
        doThrow(new IOException("write")).when(baos).write(anyInt());
        doThrow(new IOException("write")).when(baos).write(any(byte[].class), anyInt(), anyInt());

        out  = new Base64OutputStream(baos, Base64.NO_CLOSE);
        out.write(33);
        try {
            out.close();
            fail();
        } catch (IOException e) {
            assertThat(e.getMessage(), is("write"));
        }
    }

    @Test
    public void testRoundtrip_1() throws IOException {
        Random random = new Random();
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(buf, Base64.CRLF);
        out.write(bytes);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(buf.toByteArray());
        Base64InputStream in = new Base64InputStream(bais, Base64.CRLF);
        byte[] bytes2 = new byte[64];
        in.read(bytes2);

        assertThat(bytes2, is(bytes));
    }

    @Test
    public void testRoundtrip_2() throws IOException {
        Random random = new Random();
        byte[] bytes = new byte[59];
        random.nextBytes(bytes);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        Base64OutputStream out = new Base64OutputStream(buf, Base64.DEFAULT);
        out.write(bytes);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(buf.toByteArray());
        Base64InputStream in = new Base64InputStream(bais, Base64.DEFAULT);
        byte[] bytes2 = new byte[bytes.length];
        in.read(bytes2);

        assertThat(bytes2, is(bytes));
    }
}
