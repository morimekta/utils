package net.morimekta.util.io;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Testing for the ByteBufferInputStream and ByteBufferOutputStream.
 */
public class ByteBufferIOTest {
    @Test
    public void testHeapBuffer() throws IOException {
        byte[] data = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4};

        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.limit(20);
        ByteBufferOutputStream bbos = new ByteBufferOutputStream(buffer);

        bbos.write(0);
        bbos.write(data);
        bbos.write(data, 6, 2);

        buffer.flip();
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);

        assertEquals(0, bbis.read());
        byte[] indata = new byte[data.length];
        assertEquals(16, bbis.read(indata));
        assertArrayEquals(data, indata);
        byte[] indata2 = new byte[data.length];
        assertEquals(2, bbis.read(indata2, 6, 2));
        assertArrayEquals(new byte[]{0, 0, 0, 0, 0, 0, 3, 2, 0, 0, 0, 0, 0, 0, 0, 0},
                          indata2);
    }
}
