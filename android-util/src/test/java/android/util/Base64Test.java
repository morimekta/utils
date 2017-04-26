/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for base64 utilities. Updated from Android to match the current
 * implementation of Base64.
 */
public class Base64Test extends TestCase {
    /** Decodes a string, returning a string. */
    private String decodeString(String in) throws Exception {
        byte[] out = Base64.decode(in, 0);
        return new String(out);
    }

    /**
     * Encodes the string 'in' using 'flags'.  Asserts that decoding
     * gives the same string.  Returns the encoded string.
     */
    private String encodeToString(String in, int flags) throws Exception {
        String b64 = Base64.encodeToString(in.getBytes(), flags);
        String dec = decodeString(b64);
        assertEquals(in, dec);
        return b64;
    }

    /** Assert that decoding 'in' throws IllegalArgumentException. */
    private void assertBad(String ex,
                           String in) {
        try {
            byte[] out = Base64.decode(in, 0);
            fail("should have failed to decode");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals(ex)) {
                e.printStackTrace();
            }
            assertThat(e.getMessage(), is(ex));
        }
    }

    /** Assert that actual equals the first len bytes of expected. */
    private void assertEquals(byte[] expected, int len, byte[] actual) {
        assertEquals(len, actual.length);
        for (int i = 0; i < len; ++i) {
            assertEquals(expected[i], actual[i]);
        }
    }


    @Test
    public void testDecodeBad() {
        assertBad("Bad Base64 character '.' in array position 16",
                  "With punctuation.");
        assertBad("Bad Base64 character '\\\"' in array position 0",
                  "\"QUOTEDbase64\"");
        assertBad("Bad Base64 character '\\'' in array position 0",
                  "\'");
        assertBad("Bad Base64 character '\\u00a5' in array position 1",
                  "å");
        assertBad("Bad Base64 character '='",
                  "Mis=matched padding=");
        assertBad("Bad Base64 character '='",
                  // Too much padding
                  "YWFhYWFhYWE==");
        assertBad("Input string was null.",
                  null);

        try {
            Base64.decode("Boo".getBytes(UTF_8), -1, 3, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Source array with length 3 cannot have offset of -1 and process 3 bytes."));
        }
        try {
            Base64.decode("Boo".getBytes(UTF_8), 0, 5, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Source array with length 3 cannot have offset of 0 and process 5 bytes."));
        }
        try {
            Base64.decode(null, 0, 3, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Cannot decode null source array."));
        }
        try {
            Base64.decode((byte[]) null, 0);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(),
                       is("Cannot decode null source array."));
        }
    }

    @Test
    public void testConstructor() throws
                                  NoSuchMethodException,
                                  IllegalAccessException,
                                  InvocationTargetException,
                                  InstantiationException {
        Constructor<Base64> c = Base64.class.getDeclaredConstructor();
        assertThat(c.isAccessible(), is(false));
        c.setAccessible(true);
        assertThat(c.newInstance(), is(instanceOf(Base64.class)));
        c.setAccessible(false);
    }

    @Test
    public void testDecodeExtraChars() throws Exception {
        // padding 0
        assertEquals("hello, world", decodeString("aGVsbG8sIHdvcmxk"));
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxk=");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxk==");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxk =");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxk = = ");
        assertBad("Bad Base64 character '_' in array position 0", "_a*G_V*s_b*G_8*s_I*H_d*v_c*m_x*k_");
        assertEquals("hello, world", decodeString(" aGVs bG8s IHdv cmxk  "));
        assertEquals("hello, world", decodeString(" aGV sbG8 sIHd vcmx k "));
        assertEquals("hello, world", decodeString(" aG VsbG 8sIH dvcm xk "));
        assertEquals("hello, world", decodeString(" a GVsb G8sI Hdvc mxk "));
        assertEquals("hello, world", decodeString(" a G V s b G 8 s I H d v c m x k "));
        assertEquals("hello, world", decodeString("aGVsbG8sIHdvcmxk"));

        // padding 1
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPyE="));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPyE"));
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkPyE==");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkPyE ==");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkPyE = = ");
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E="));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E"));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E ="));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E "));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E = "));
        assertEquals("hello, world?!", decodeString("aGVsbG8sIHdvcmxkPy E   "));

        // padding 2
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkLg=="));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkLg"));
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkLg=");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkLg =");
        assertBad("Bad Base64 character '='", "aGVsbG8sIHdvcmxkLg = ");
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g=="));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g"));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g =="));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g "));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g = = "));
        assertEquals("hello, world.", decodeString("aGVsbG8sIHdvcmxkL g   "));
    }

    private static final byte[] BYTES = { (byte) 0xff, (byte) 0xee, (byte) 0xdd,
            (byte) 0xcc, (byte) 0xbb, (byte) 0xaa,
            (byte) 0x99, (byte) 0x88, (byte) 0x77 };

    @Test
    public void testBinaryDecode() throws Exception {
        assertEquals(BYTES, 0, Base64.decode("", 0));
        assertEquals(BYTES, 1, Base64.decode("/w", 0));
        assertEquals(BYTES, 1, Base64.decode("/w==", 0));
        assertEquals(BYTES, 2, Base64.decode("/+4", 0));
        assertEquals(BYTES, 2, Base64.decode("/+4=", 0));
        assertEquals(BYTES, 3, Base64.decode("/+7d", 0));
        assertEquals(BYTES, 4, Base64.decode("/+7dzA", 0));
        assertEquals(BYTES, 4, Base64.decode("/+7dzA==", 0));
        assertEquals(BYTES, 5, Base64.decode("/+7dzLs", 0));
        assertEquals(BYTES, 5, Base64.decode("/+7dzLs=", 0));
        assertEquals(BYTES, 6, Base64.decode("/+7dzLuq", 0));
        assertEquals(BYTES, 7, Base64.decode("/+7dzLuqmQ", 0));
        assertEquals(BYTES, 7, Base64.decode("/+7dzLuqmQ==", 0));
        assertEquals(BYTES, 8, Base64.decode("/+7dzLuqmYg", 0));
        assertEquals(BYTES, 8, Base64.decode("/+7dzLuqmYg=", 0));
    }

    @Test
    public void testWebSafe() throws Exception {
        assertEquals(BYTES, 0, Base64.decode("", Base64.URL_SAFE));
        assertEquals(BYTES, 1, Base64.decode("_w", Base64.URL_SAFE));
        assertEquals(BYTES, 1, Base64.decode("_w==", Base64.URL_SAFE));
        assertEquals(BYTES, 2, Base64.decode("_-4", Base64.URL_SAFE));
        assertEquals(BYTES, 2, Base64.decode("_-4=", Base64.URL_SAFE));
        assertEquals(BYTES, 3, Base64.decode("_-7d", Base64.URL_SAFE));
        assertEquals(BYTES, 4, Base64.decode("_-7dzA", Base64.URL_SAFE));
        assertEquals(BYTES, 4, Base64.decode("_-7dzA==", Base64.URL_SAFE));
        assertEquals(BYTES, 5, Base64.decode("_-7dzLs", Base64.URL_SAFE));
        assertEquals(BYTES, 5, Base64.decode("_-7dzLs=", Base64.URL_SAFE));
        assertEquals(BYTES, 6, Base64.decode("_-7dzLuq", Base64.URL_SAFE));
        assertEquals(BYTES, 7, Base64.decode("_-7dzLuqmQ", Base64.URL_SAFE));
        assertEquals(BYTES, 8, Base64.decode("_-7dzLuqmYg", Base64.URL_SAFE));

        assertEquals("", Base64.encodeToString(BYTES, 0, 0, Base64.URL_SAFE));
        assertEquals("_w==", Base64.encodeToString(BYTES, 0, 1, Base64.URL_SAFE));
        assertEquals("_-4=", Base64.encodeToString(BYTES, 0, 2, Base64.URL_SAFE));
        assertEquals("_-7d", Base64.encodeToString(BYTES, 0, 3, Base64.URL_SAFE));
        assertEquals("_-7dzA==", Base64.encodeToString(BYTES, 0, 4, Base64.URL_SAFE));
        assertEquals("_-7dzLs=", Base64.encodeToString(BYTES, 0, 5, Base64.URL_SAFE));
        assertEquals("_-7dzLuq", Base64.encodeToString(BYTES, 0, 6, Base64.URL_SAFE));
        assertEquals("_-7dzLuqmQ==", Base64.encodeToString(BYTES, 0, 7, Base64.URL_SAFE));
        assertEquals("_-7dzLuqmYg=", Base64.encodeToString(BYTES, 0, 8, Base64.URL_SAFE));

        assertEquals("", Base64.encodeToString(BYTES, 0, 0, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_w", Base64.encodeToString(BYTES, 0, 1, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-4", Base64.encodeToString(BYTES, 0, 2, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7d", Base64.encodeToString(BYTES, 0, 3, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7dzA", Base64.encodeToString(BYTES, 0, 4, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7dzLs", Base64.encodeToString(BYTES, 0, 5, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7dzLuq", Base64.encodeToString(BYTES, 0, 6, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7dzLuqmQ", Base64.encodeToString(BYTES, 0, 7, Base64.URL_SAFE | Base64.NO_PADDING));
        assertEquals("_-7dzLuqmYg", Base64.encodeToString(BYTES, 0, 8, Base64.URL_SAFE | Base64.NO_PADDING));
    }

    @Test
    public void testFlags() throws Exception {
        assertEquals("YQ==",         encodeToString("a", Base64.DEFAULT));
        assertEquals("YQ==",         encodeToString("a", Base64.NO_WRAP));
        assertEquals("YQ",           encodeToString("a", Base64.NO_PADDING));
        assertEquals("YQ",           encodeToString("a", Base64.NO_PADDING | Base64.NO_WRAP));
        assertEquals("YQ==",         encodeToString("a", Base64.CRLF));
        assertEquals("YQ",           encodeToString("a", Base64.CRLF | Base64.NO_PADDING));

        assertEquals("YWI=",         encodeToString("ab", Base64.DEFAULT));
        assertEquals("YWI=",         encodeToString("ab", Base64.NO_WRAP));
        assertEquals("YWI",          encodeToString("ab", Base64.NO_PADDING));
        assertEquals("YWI",          encodeToString("ab", Base64.NO_PADDING | Base64.NO_WRAP));
        assertEquals("YWI=",         encodeToString("ab", Base64.CRLF));
        assertEquals("YWI",          encodeToString("ab", Base64.CRLF | Base64.NO_PADDING));

        assertEquals("YWJj",         encodeToString("abc", Base64.DEFAULT));
        assertEquals("YWJj",         encodeToString("abc", Base64.NO_WRAP));
        assertEquals("YWJj",         encodeToString("abc", Base64.NO_PADDING));
        assertEquals("YWJj",         encodeToString("abc", Base64.NO_PADDING | Base64.NO_WRAP));
        assertEquals("YWJj",         encodeToString("abc", Base64.CRLF));
        assertEquals("YWJj",         encodeToString("abc", Base64.CRLF | Base64.NO_PADDING));

        assertEquals("YWJjZA==",     encodeToString("abcd", Base64.DEFAULT));
        assertEquals("YWJjZA==",     encodeToString("abcd", Base64.NO_WRAP));
        assertEquals("YWJjZA",       encodeToString("abcd", Base64.NO_PADDING));
        assertEquals("YWJjZA",       encodeToString("abcd", Base64.NO_PADDING | Base64.NO_WRAP));
        assertEquals("YWJjZA==",     encodeToString("abcd", Base64.CRLF));
        assertEquals("YWJjZA",       encodeToString("abcd", Base64.CRLF | Base64.NO_PADDING));
    }

    @Test
    public void testLineLength() throws Exception {
        String in_56 = "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd";
        String in_57 = in_56 + "e";
        String in_58 = in_56 + "ef";
        String in_59 = in_56 + "efg";
        String in_60 = in_56 + "efgh";
        String in_61 = in_56 + "efghi";

        String prefix = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXphYmNkZWZnaGlqa2xtbm9wcXJzdHV2d3h5emFi";
        String out_56 = prefix + "Y2Q=";
        String out_57 = prefix + "Y2Rl";
        String out_58 = prefix + "Y2Rl\nZg==";
        String out_59 = prefix + "Y2Rl\nZmc=";
        String out_60 = prefix + "Y2Rl\nZmdo";
        String out_61 = prefix + "Y2Rl\nZmdoaQ==";

        // no newline for an empty input array.
        assertEquals("", encodeToString("", 0));

        assertEquals(out_56, encodeToString(in_56, 0));
        assertEquals(out_57, encodeToString(in_57, 0));
        assertEquals(out_58, encodeToString(in_58, 0));
        assertEquals(out_59, encodeToString(in_59, 0));
        assertEquals(out_60, encodeToString(in_60, 0));
        assertEquals(out_61, encodeToString(in_61, 0));

        assertEquals(out_56.replaceAll("[=]", ""), encodeToString(in_56, Base64.NO_PADDING));
        assertEquals(out_57.replaceAll("[=]", ""), encodeToString(in_57, Base64.NO_PADDING));
        assertEquals(out_58.replaceAll("[=]", ""), encodeToString(in_58, Base64.NO_PADDING));
        assertEquals(out_59.replaceAll("[=]", ""), encodeToString(in_59, Base64.NO_PADDING));
        assertEquals(out_60.replaceAll("[=]", ""), encodeToString(in_60, Base64.NO_PADDING));
        assertEquals(out_61.replaceAll("[=]", ""), encodeToString(in_61, Base64.NO_PADDING));

        assertEquals(out_56.replaceAll("\\n", ""), encodeToString(in_56, Base64.NO_WRAP));
        assertEquals(out_57.replaceAll("\\n", ""), encodeToString(in_57, Base64.NO_WRAP));
        assertEquals(out_58.replaceAll("\\n", ""), encodeToString(in_58, Base64.NO_WRAP));
        assertEquals(out_59.replaceAll("\\n", ""), encodeToString(in_59, Base64.NO_WRAP));
        assertEquals(out_60.replaceAll("\\n", ""), encodeToString(in_60, Base64.NO_WRAP));
        assertEquals(out_61.replaceAll("\\n", ""), encodeToString(in_61, Base64.NO_WRAP));

        assertEquals(out_56.replaceAll("[\\n=]", ""), encodeToString(in_56, Base64.NO_WRAP | Base64.NO_PADDING));
        assertEquals(out_57.replaceAll("[\\n=]", ""), encodeToString(in_57, Base64.NO_WRAP | Base64.NO_PADDING));
        assertEquals(out_58.replaceAll("[\\n=]", ""), encodeToString(in_58, Base64.NO_WRAP | Base64.NO_PADDING));
        assertEquals(out_59.replaceAll("[\\n=]", ""), encodeToString(in_59, Base64.NO_WRAP | Base64.NO_PADDING));
        assertEquals(out_60.replaceAll("[\\n=]", ""), encodeToString(in_60, Base64.NO_WRAP | Base64.NO_PADDING));
        assertEquals(out_61.replaceAll("[\\n=]", ""), encodeToString(in_61, Base64.NO_WRAP | Base64.NO_PADDING));

        assertEquals(out_56, encodeToString(in_56, Base64.URL_SAFE));
        assertEquals(out_57, encodeToString(in_57, Base64.URL_SAFE));
        assertEquals(out_58, encodeToString(in_58, Base64.URL_SAFE));
        assertEquals(out_59, encodeToString(in_59, Base64.URL_SAFE));
        assertEquals(out_60, encodeToString(in_60, Base64.URL_SAFE));
        assertEquals(out_61, encodeToString(in_61, Base64.URL_SAFE));

        assertEquals(out_56.replaceAll("\\n", "\r\n"), encodeToString(in_56, Base64.CRLF));
        assertEquals(out_57.replaceAll("\\n", "\r\n"), encodeToString(in_57, Base64.CRLF));
        assertEquals(out_58.replaceAll("\\n", "\r\n"), encodeToString(in_58, Base64.CRLF));
        assertEquals(out_59.replaceAll("\\n", "\r\n"), encodeToString(in_59, Base64.CRLF));
        assertEquals(out_60.replaceAll("\\n", "\r\n"), encodeToString(in_60, Base64.CRLF));
        assertEquals(out_61.replaceAll("\\n", "\r\n"), encodeToString(in_61, Base64.CRLF));
    }

    private static final String lipsum =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                    "Quisque congue eleifend odio, eu ornare nulla facilisis eget. " +
                    "Integer eget elit diam, sit amet laoreet nibh. Quisque enim " +
                    "urna, pharetra vitae consequat eget, adipiscing eu ante. " +
                    "Aliquam venenatis arcu nec nibh imperdiet tempor. In id dui " +
                    "eget lorem aliquam rutrum vel vitae eros. In placerat ornare " +
                    "pretium. Curabitur non fringilla mi. Fusce ultricies, turpis " +
                    "eu ultrices suscipit, ligula nisi consectetur eros, dapibus " +
                    "aliquet dui sapien a turpis. Donec ultricies varius ligula, " +
                    "ut hendrerit arcu malesuada at. Praesent sed elit pretium " +
                    "eros luctus gravida. In ac dolor lorem. Cras condimentum " +
                    "convallis elementum. Phasellus vel felis in nulla ultrices " +
                    "venenatis. Nam non tortor non orci convallis convallis. " +
                    "Nam tristique lacinia hendrerit. Pellentesque habitant morbi " +
                    "tristique senectus et netus et malesuada fames ac turpis " +
                    "egestas. Vivamus cursus, nibh eu imperdiet porta, magna " +
                    "ipsum mollis mauris, sit amet fringilla mi nisl eu mi. " +
                    "Phasellus posuere, leo at ultricies vehicula, massa risus " +
                    "volutpat sapien, eu tincidunt diam ipsum eget nulla. Cras " +
                    "molestie dapibus commodo. Ut vel tellus at massa gravida " +
                    "semper non sed orci.";

    @Test
    public void testInputStream() throws Exception {
        int[] flagses = { Base64.DEFAULT,
                Base64.NO_PADDING,
                Base64.NO_WRAP,
                Base64.NO_PADDING | Base64.NO_WRAP,
                Base64.CRLF,
                Base64.URL_SAFE };
        int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };
        Random rng = new Random(32176L);

        // Test input needs to be at least 2048 bytes to fill up the
        // read buffer of Base64InputStream.
        byte[] plain = (lipsum + lipsum + lipsum + lipsum + lipsum).getBytes();

        for (int flags: flagses) {
            byte[] encoded = Base64.encode(plain, flags);

            ByteArrayInputStream bais;
            Base64InputStream b64is;
            byte[] actual = new byte[plain.length * 2];
            int ap;
            int b;

            // ----- test decoding ("encoded" -> "plain") -----

            // read as much as it will give us in one chunk
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, flags);
            ap = 0;
            while ((b = b64is.read(actual, ap, actual.length-ap)) != -1) {
                ap += b;
            }
            assertEquals(new String(actual, 0, ap, UTF_8), new String(plain, UTF_8));

            // read individual bytes
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, flags);
            ap = 0;
            while ((b = b64is.read()) != -1) {
                actual[ap++] = (byte) b;
            }
            assertEquals(new String(actual, 0, ap, UTF_8), new String(plain, UTF_8));

            // mix reads of variously-sized arrays with one-byte reads
            bais = new ByteArrayInputStream(encoded);
            b64is = new Base64InputStream(bais, flags);
            ap = 0;
            readloop: while (true) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                if (l >= 0) {
                    b = b64is.read(actual, ap, l);
                    if (b == -1) break readloop;
                    ap += b;
                } else {
                    for (int i = 0; i < -l; ++i) {
                        if ((b = b64is.read()) == -1) break readloop;
                        actual[ap++] = (byte) b;
                    }
                }
            }
            assertEquals(new String(actual, 0, ap, UTF_8), new String(plain, UTF_8));
        }
    }

    /**
     * Tests that Base64OutputStream produces exactly the same results
     * as calling Base64.encode on an in-memory array.
     */
    @Test
    public void testOutputStream() throws Exception {
        int[] flagses = { Base64.DEFAULT,
                Base64.NO_PADDING,
                Base64.NO_WRAP,
                Base64.NO_PADDING | Base64.NO_WRAP,
                Base64.CRLF,
                Base64.URL_SAFE };
        int[] writeLengths = { -10, -5, -1, 0, 1, 1, 2, 2, 3, 10, 100 };
        Random rng = new Random(32176L);

        // Test input needs to be at least 1024 bytes to test filling
        // up the write(int) buffer of Base64OutputStream.
        byte[] plain = (lipsum + lipsum).getBytes(UTF_8);

        for (int flags : flagses) {
            byte[] encoded = Base64.encode(plain, flags);

            ByteArrayOutputStream baos;
            Base64OutputStream b64os;
            byte[] actual;
            int p;

            // ----- test encoding ("plain" -> "encoded") -----

            // one large write(byte[]) of the whole input
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, flags);
            b64os.write(plain);
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(new String(encoded, UTF_8), new String(actual, UTF_8));

            // many calls to write(int)
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, flags);
            for (int i = 0; i < plain.length; ++i) {
                b64os.write(plain[i]);
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(new String(encoded, UTF_8), new String(actual, UTF_8));

            // intermixed sequences of write(int) with
            // write(byte[],int,int) of various lengths.
            baos = new ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, flags);
            p = 0;
            while (p < plain.length) {
                int l = writeLengths[rng.nextInt(writeLengths.length)];
                l = Math.min(l, plain.length-p);
                if (l >= 0) {
                    b64os.write(plain, p, l);
                    p += l;
                } else {
                    l = Math.min(-l, plain.length-p);
                    for (int i = 0; i < l; ++i) {
                        b64os.write(plain[p+i]);
                    }
                    p += l;
                }
            }
            b64os.close();
            actual = baos.toByteArray();
            assertEquals(new String(encoded, UTF_8), new String(actual, UTF_8));
        }
    }
}
