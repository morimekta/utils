package net.morimekta.util.io;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Test
    public void testDirectBuffer() throws IOException {
        byte[] data = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4};

        ByteBuffer buffer = ByteBuffer.allocateDirect(20);
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

    @Test
    public void testMarkReset() throws IOException {
        byte[] data = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4};
        ByteBuffer buffer = ByteBuffer.wrap(data);

        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);

        assertTrue(bbis.markSupported());

        assertEquals(9, bbis.read());
        assertEquals(8, bbis.read());

        // The "mark limit" is irrelevant.
        bbis.mark(0);

        assertEquals(7, bbis.read());
        assertEquals(6, bbis.read());
        assertEquals(5, bbis.read());

        bbis.reset();

        assertEquals(7, bbis.read());
        assertEquals(6, bbis.read());

        bbis.reset();

        assertEquals(7, bbis.read());
        assertEquals(6, bbis.read());
        assertEquals(5, bbis.read());
    }

    @Test
    public void testNoSuchMark() {
        byte[] data = new byte[]{9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 9, 8, 7, 6, 5, 4};
        ByteBuffer buffer = ByteBuffer.wrap(data);

        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);

        try {
            bbis.reset();
            fail("No exception on reset without mark.");
        } catch (IOException e) {
            assertEquals("No mark set on stream", e.getMessage());
        }
    }

    @Test
    public void testReadLessThanMax_array() throws IOException {
        byte[] data = new byte[]{5, 4};
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);

        byte[] out = new byte[10];

        assertEquals(2, bbis.read(out));
        assertArrayEquals(new byte[]{5, 4, 0, 0, 0, 0, 0, 0, 0, 0}, out);

        assertEquals(-1, bbis.read(out));
        assertEquals(-1, bbis.read());

    }

    @Test
    public void testReadLessThanMax_arrayWithOffset() throws IOException {
        byte[] data = new byte[]{5, 4};
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);

        byte[] out = new byte[10];

        assertEquals(2, bbis.read(out, 2, 5));
        assertArrayEquals(new byte[]{0, 0, 5, 4, 0, 0, 0, 0, 0, 0}, out);

        assertEquals(-1, bbis.read(out, 2, 5));
        assertEquals(-1, bbis.read());
    }

    @Test
    public void testWriteOverBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        ByteBufferOutputStream bbos = new ByteBufferOutputStream(buffer);

        try {
            bbos.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0});
            fail("No exception on writing too much");
        } catch (IOException e) {
            assertEquals("Buffer overflow", e.getMessage());
        }
        try {
            bbos.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0}, 5, 12);
            fail("No exception on writing too much");
        } catch (IOException e) {
            assertEquals("Buffer overflow", e.getMessage());
        }

        bbos.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0});

        try {
            bbos.write(5);
            fail("No exception on writing too much");
        } catch (IOException e) {
            assertEquals("Buffer overflow", e.getMessage());
        }
    }

    @Test
    public void testNegativeBytes() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{0, 1, -1, 2, -2, 3, -3, 4, -4, 5});
        buffer.flip();

        ByteBufferInputStream bbis = new ByteBufferInputStream(buffer);
        assertThat(bbis.read(), is(0));
        assertThat(bbis.read(), is(1));
        assertThat(bbis.read(), is(255));
        assertThat(bbis.read(), is(2));
        assertThat(bbis.read(), is(254));
        assertThat(bbis.read(), is(3));
        assertThat(bbis.read(), is(253));
        assertThat(bbis.read(), is(4));
        assertThat(bbis.read(), is(252));
        assertThat(bbis.read(), is(5));
        assertThat(bbis.read(), is(-1));
    }
}
