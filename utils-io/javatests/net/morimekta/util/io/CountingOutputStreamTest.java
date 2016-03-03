package net.morimekta.util.io;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Created by morimekta on 3/3/16.
 */
public class CountingOutputStreamTest {
    @Test
    public void testWrite() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountingOutputStream out = new CountingOutputStream(baos);

        int[] test = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -2, -50, -100, 2, 50, 100, 4000};
        Random random = new Random(System.nanoTime());

        byte[] b4095 = new byte[4096];

        int num = 0;
        for (int i = 0; i < 100; ++i) {
            int p = random.nextInt(test.length);
            int n = test[p];
            if (n == 1) {
                out.write(0x11);
                ++num;
            } else if (n < 0) {
                byte[] buffer = new byte[-n];
                out.write(buffer);

                num -= n;
            } else {
                out.write(b4095, 0, n);
                num += n;
            }
        }

        byte[] result = baos.toByteArray();

        assertEquals(result.length, num);
        assertEquals(result.length, out.getByteCount());
    }

    @Test
    public void testFlush() throws IOException {
        OutputStream out = mock(OutputStream.class);
        CountingOutputStream co = new CountingOutputStream(out);

        co.flush();
        verify(out).flush();
        verifyNoMoreInteractions(out);
    }

    @Test
    public void testClose() throws IOException {
        OutputStream out = mock(OutputStream.class);
        CountingOutputStream co = new CountingOutputStream(out);

        co.close();
        verify(out).close();
        verifyNoMoreInteractions(out);
    }
}
