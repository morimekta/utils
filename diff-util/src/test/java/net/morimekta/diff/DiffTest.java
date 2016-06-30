/*
 * Test harness for match_patch.java
 *
 * Copyright 2006 Google Inc.
 * http://code.google.com/p/google-diff-match-patch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.morimekta.diff;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DiffTest {
    private Diff dmp;
    private Diff.Operation DELETE = Diff.Operation.DELETE;
    private Diff.Operation EQUAL  = Diff.Operation.EQUAL;
    private Diff.Operation INSERT = Diff.Operation.INSERT;

    @Before
    public void setUp() {
        // Create an instance of the match_patch object.
        dmp = new Diff("", "", Diff.Options.defaults()
                                           .withTimeout(1)
                                           .withEditCost(4));
    }

    //  DIFF TEST FUNCTIONS

    @Test
    public void testDiffHalfmatch() {
        // Detect a halfmatch.
        Assert.assertNull("halfMatch: No match #1.", dmp.halfMatch("1234567890", "abcdef"));

        Assert.assertNull("halfMatch: No match #2.", dmp.halfMatch("12345", "23"));

        assertArrayEquals("halfMatch: Single Match #1.", new String[]{"12", "90", "a", "z", "345678"}, dmp.halfMatch("1234567890", "a345678z"));

        assertArrayEquals("halfMatch: Single Match #2.", new String[]{"a", "z", "12", "90", "345678"}, dmp.halfMatch("a345678z", "1234567890"));

        assertArrayEquals("halfMatch: Single Match #3.", new String[]{"abc", "z", "1234", "0", "56789"}, dmp.halfMatch("abc56789z", "1234567890"));

        assertArrayEquals("halfMatch: Single Match #4.", new String[]{"a", "xyz", "1", "7890", "23456"}, dmp.halfMatch("a23456xyz", "1234567890"));

        assertArrayEquals("halfMatch: Multiple Matches #1.", new String[]{"12123", "123121", "a", "z", "1234123451234"}, dmp.halfMatch("121231234123451234123121", "a1234123451234z"));

        assertArrayEquals("halfMatch: Multiple Matches #2.", new String[]{"", "-=-=-=-=-=", "x", "", "x-=-=-=-=-=-=-="}, dmp.halfMatch("x-=-=-=-=-=-=-=-=-=-=-=-=", "xx-=-=-=-=-=-=-="));

        assertArrayEquals("halfMatch: Multiple Matches #3.", new String[]{"-=-=-=-=-=", "", "", "y", "-=-=-=-=-=-=-=y"}, dmp.halfMatch("-=-=-=-=-=-=-=-=-=-=-=-=y", "-=-=-=-=-=-=-=yy"));

        // Optimal diff would be -q+x=H-i+e=lloHe+Hu=llo-Hew+y not -qHillo+x=HelloHe-w+Hulloy
        assertArrayEquals("halfMatch: Non-optimal halfmatch.", new String[]{"qHillo", "w", "x", "Hulloy", "HelloHe"}, dmp.halfMatch("qHilloHelloHew", "xHelloHeHulloy"));

        dmp = new Diff("", "", Diff.Options.defaults().withTimeout(0));
        Assert.assertNull("halfMatch: Optimal no halfmatch.", dmp.halfMatch("qHilloHelloHew", "xHelloHeHulloy"));
    }

    @Test
    public void testDiffLinesToChars() {
        // Convert lines down to characters.
        ArrayList<String> tmpVector = new ArrayList<String>();
        tmpVector.add("");
        tmpVector.add("alpha\n");
        tmpVector.add("beta\n");
        assertLinesToCharsResultEquals("linesToChars: Shared lines.", new Diff.LinesToCharsResult("\u0001\u0002\u0001", "\u0002\u0001\u0002", tmpVector), dmp.linesToChars("alpha\nbeta\nalpha\n", "beta\nalpha\nbeta\n"));

        tmpVector.clear();
        tmpVector.add("");
        tmpVector.add("alpha\r\n");
        tmpVector.add("beta\r\n");
        tmpVector.add("\r\n");
        assertLinesToCharsResultEquals("linesToChars: Empty string and blank lines.", new Diff.LinesToCharsResult("", "\u0001\u0002\u0003\u0003", tmpVector), dmp.linesToChars("", "alpha\r\nbeta\r\n\r\n\r\n"));

        tmpVector.clear();
        tmpVector.add("");
        tmpVector.add("a");
        tmpVector.add("b");
        assertLinesToCharsResultEquals("linesToChars: No linebreaks.", new Diff.LinesToCharsResult("\u0001", "\u0002", tmpVector), dmp.linesToChars("a", "b"));

        // More than 256 to reveal any 8-bit limitations.
        int n = 300;
        tmpVector.clear();
        StringBuilder lineList = new StringBuilder();
        StringBuilder charList = new StringBuilder();
        for (int x = 1; x < n + 1; x++) {
            tmpVector.add(x + "\n");
            lineList.append(x + "\n");
            charList.append(String.valueOf((char) x));
        }
        Assert.assertEquals(n, tmpVector.size());
        String lines = lineList.toString();
        String chars = charList.toString();
        Assert.assertEquals(n, chars.length());
        tmpVector.add(0, "");
        assertLinesToCharsResultEquals("linesToChars: More than 256.", new Diff.LinesToCharsResult(chars, "", tmpVector), dmp.linesToChars(lines, ""));
    }

    @Test
    public void testDiffCharsToLines() {
        // First check that Diff equality works.
        Assert.assertTrue("charsToLines: Equality #1.", new Diff.Change(EQUAL, "a").equals(new Diff.Change(EQUAL, "a")));

        Assert.assertEquals("charsToLines: Equality #2.", new Diff.Change(EQUAL, "a"), new Diff.Change(EQUAL, "a"));

        // Convert chars up to lines.
        LinkedList<Diff.Change> diffs = diffList(new Diff.Change(EQUAL, "\u0001\u0002\u0001"), new Diff.Change(INSERT, "\u0002\u0001\u0002"));
        ArrayList<String> tmpVector = new ArrayList<String>();
        tmpVector.add("");
        tmpVector.add("alpha\n");
        tmpVector.add("beta\n");
        dmp.charsToLines(diffs, tmpVector);
        Assert.assertEquals("charsToLines: Shared lines.", diffList(new Diff.Change(EQUAL, "alpha\nbeta\nalpha\n"), new Diff.Change(INSERT, "beta\nalpha\nbeta\n")), diffs);

        // More than 256 to reveal any 8-bit limitations.
        int n = 300;
        tmpVector.clear();
        StringBuilder lineList = new StringBuilder();
        StringBuilder charList = new StringBuilder();
        for (int x = 1; x < n + 1; x++) {
            tmpVector.add(x + "\n");
            lineList.append(x + "\n");
            charList.append(String.valueOf((char) x));
        }
        Assert.assertEquals(n, tmpVector.size());
        String lines = lineList.toString();
        String chars = charList.toString();
        Assert.assertEquals(n, chars.length());
        tmpVector.add(0, "");
        diffs = diffList(new Diff.Change(DELETE, chars));
        dmp.charsToLines(diffs, tmpVector);
        Assert.assertEquals("charsToLines: More than 256.", diffList(new Diff.Change(DELETE, lines)), diffs);
    }

    @Test
    public void testDiffCleanupMerge() {
        // Cleanup a messy diff.
        LinkedList<Diff.Change> diffs = diffList();
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Null case.", diffList(), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "b"), new Diff.Change(INSERT, "c"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: No change case.", diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "b"), new Diff.Change(INSERT, "c")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(EQUAL, "b"), new Diff.Change(EQUAL, "c"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Merge equalities.", diffList(new Diff.Change(EQUAL, "abc")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "a"), new Diff.Change(DELETE, "b"), new Diff.Change(DELETE, "c"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Merge deletions.", diffList(new Diff.Change(DELETE, "abc")), diffs);

        diffs = diffList(new Diff.Change(INSERT, "a"), new Diff.Change(INSERT, "b"), new Diff.Change(INSERT, "c"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Merge insertions.", diffList(new Diff.Change(INSERT, "abc")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "b"), new Diff.Change(DELETE, "c"), new Diff.Change(INSERT, "d"), new Diff.Change(EQUAL, "e"), new Diff.Change(EQUAL, "f"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Merge interweave.", diffList(new Diff.Change(DELETE, "ac"), new Diff.Change(INSERT, "bd"), new Diff.Change(EQUAL, "ef")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "abc"), new Diff.Change(DELETE, "dc"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Prefix and suffix detection.", diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "d"), new Diff.Change(INSERT, "b"), new Diff.Change(EQUAL, "c")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "x"), new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "abc"), new Diff.Change(DELETE, "dc"), new Diff.Change(EQUAL, "y"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Prefix and suffix detection with equalities.", diffList(new Diff.Change(EQUAL, "xa"), new Diff.Change(DELETE, "d"), new Diff.Change(INSERT, "b"), new Diff.Change(EQUAL, "cy")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(INSERT, "ba"), new Diff.Change(EQUAL, "c"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Slide edit left.", diffList(new Diff.Change(INSERT, "ab"), new Diff.Change(EQUAL, "ac")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "c"), new Diff.Change(INSERT, "ab"), new Diff.Change(EQUAL, "a"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Slide edit right.", diffList(new Diff.Change(EQUAL, "ca"), new Diff.Change(INSERT, "ba")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "b"), new Diff.Change(EQUAL, "c"), new Diff.Change(DELETE, "ac"), new Diff.Change(EQUAL, "x"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Slide edit left recursive.", diffList(new Diff.Change(DELETE, "abc"), new Diff.Change(EQUAL, "acx")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "x"), new Diff.Change(DELETE, "ca"), new Diff.Change(EQUAL, "c"), new Diff.Change(DELETE, "b"), new Diff.Change(EQUAL, "a"));
        dmp.cleanupMerge(diffs);
        Assert.assertEquals("cleanupMerge: Slide edit right recursive.", diffList(new Diff.Change(EQUAL, "xca"), new Diff.Change(DELETE, "cba")), diffs);
    }

    @Test
    public void testDiffCleanupSemanticLossless() {
        // Slide diffs to match logical boundaries.
        LinkedList<Diff.Change> diffs = diffList();
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Null case.", diffList(), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "AAA\r\n\r\nBBB"), new Diff.Change(INSERT, "\r\nDDD\r\n\r\nBBB"), new Diff.Change(EQUAL, "\r\nEEE"));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Blank lines.", diffList(new Diff.Change(EQUAL, "AAA\r\n\r\n"), new Diff.Change(INSERT, "BBB\r\nDDD\r\n\r\n"), new Diff.Change(EQUAL, "BBB\r\nEEE")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "AAA\r\nBBB"), new Diff.Change(INSERT, " DDD\r\nBBB"), new Diff.Change(EQUAL, " EEE"));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Line boundaries.", diffList(new Diff.Change(EQUAL, "AAA\r\n"), new Diff.Change(INSERT, "BBB DDD\r\n"), new Diff.Change(EQUAL, "BBB EEE")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "The c"), new Diff.Change(INSERT, "ow and the c"), new Diff.Change(EQUAL, "at."));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Word boundaries.", diffList(new Diff.Change(EQUAL, "The "), new Diff.Change(INSERT, "cow and the "), new Diff.Change(EQUAL, "cat.")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "The-c"), new Diff.Change(INSERT, "ow-and-the-c"), new Diff.Change(EQUAL, "at."));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Alphanumeric boundaries.", diffList(new Diff.Change(EQUAL, "The-"), new Diff.Change(INSERT, "cow-and-the-"), new Diff.Change(EQUAL, "cat.")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "a"), new Diff.Change(EQUAL, "ax"));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Hitting the start.", diffList(new Diff.Change(DELETE, "a"), new Diff.Change(EQUAL, "aax")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "xa"), new Diff.Change(DELETE, "a"), new Diff.Change(EQUAL, "a"));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Hitting the end.", diffList(new Diff.Change(EQUAL, "xaa"), new Diff.Change(DELETE, "a")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "The xxx. The "), new Diff.Change(INSERT, "zzz. The "), new Diff.Change(EQUAL, "yyy."));
        dmp.cleanupSemanticLossless(diffs);
        Assert.assertEquals("cleanupSemanticLossless: Sentence boundaries.", diffList(new Diff.Change(EQUAL, "The xxx."), new Diff.Change(INSERT, " The zzz."), new Diff.Change(EQUAL, " The yyy.")), diffs);
    }

    @Test
    public void testDiffCleanupSemantic() {
        // Cleanup semantically trivial equalities.
        LinkedList<Diff.Change> diffs = diffList();
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Null case.", diffList(), diffs);

        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "cd"), new Diff.Change(EQUAL, "12"), new Diff.Change(DELETE, "e"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: No elimination #1.", diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "cd"), new Diff.Change(EQUAL, "12"), new Diff.Change(DELETE, "e")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "abc"), new Diff.Change(INSERT, "ABC"), new Diff.Change(EQUAL, "1234"), new Diff.Change(DELETE, "wxyz"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: No elimination #2.", diffList(new Diff.Change(DELETE, "abc"), new Diff.Change(INSERT, "ABC"), new Diff.Change(EQUAL, "1234"), new Diff.Change(DELETE, "wxyz")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "a"), new Diff.Change(EQUAL, "b"), new Diff.Change(DELETE, "c"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Simple elimination.", diffList(new Diff.Change(DELETE, "abc"), new Diff.Change(INSERT, "b")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(EQUAL, "cd"), new Diff.Change(DELETE, "e"), new Diff.Change(EQUAL, "f"), new Diff.Change(INSERT, "g"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Backpass elimination.", diffList(new Diff.Change(DELETE, "abcdef"), new Diff.Change(INSERT, "cdfg")), diffs);

        diffs = diffList(new Diff.Change(INSERT, "1"), new Diff.Change(EQUAL, "A"), new Diff.Change(DELETE, "B"), new Diff.Change(INSERT, "2"), new Diff.Change(EQUAL, "_"), new Diff.Change(INSERT, "1"), new Diff.Change(EQUAL, "A"), new Diff.Change(DELETE, "B"), new Diff.Change(INSERT, "2"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Multiple elimination.", diffList(new Diff.Change(DELETE, "AB_AB"), new Diff.Change(INSERT, "1A2_1A2")), diffs);

        diffs = diffList(new Diff.Change(EQUAL, "The c"), new Diff.Change(DELETE, "ow and the c"), new Diff.Change(EQUAL, "at."));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Word boundaries.", diffList(new Diff.Change(EQUAL, "The "), new Diff.Change(DELETE, "cow and the "), new Diff.Change(EQUAL, "cat.")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "abcxx"), new Diff.Change(INSERT, "xxdef"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: No overlap elimination.", diffList(new Diff.Change(DELETE, "abcxx"), new Diff.Change(INSERT, "xxdef")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "abcxxx"), new Diff.Change(INSERT, "xxxdef"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Overlap elimination.", diffList(new Diff.Change(DELETE, "abc"), new Diff.Change(EQUAL, "xxx"), new Diff.Change(INSERT, "def")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "xxxabc"), new Diff.Change(INSERT, "defxxx"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Reverse overlap elimination.", diffList(new Diff.Change(INSERT, "def"), new Diff.Change(EQUAL, "xxx"), new Diff.Change(DELETE, "abc")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "abcd1212"), new Diff.Change(INSERT, "1212efghi"), new Diff.Change(EQUAL, "----"), new Diff.Change(DELETE, "A3"), new Diff.Change(INSERT, "3BC"));
        dmp.cleanupSemantic(diffs);
        Assert.assertEquals("cleanupSemantic: Two overlap eliminations.", diffList(new Diff.Change(DELETE, "abcd"), new Diff.Change(EQUAL, "1212"), new Diff.Change(INSERT, "efghi"), new Diff.Change(EQUAL, "----"), new Diff.Change(DELETE, "A"), new Diff.Change(EQUAL, "3"), new Diff.Change(INSERT, "BC")), diffs);
    }

    @Test
    public void testDiffCleanupEfficiency() {
        // Cleanup operationally trivial equalities.
        dmp.getOptions().withEditCost(4);
        LinkedList<Diff.Change> diffs = diffList();
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: Null case.", diffList(), diffs);

        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "wxyz"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "34"));
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: No elimination.", diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "wxyz"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "34")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "xyz"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "34"));
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: Four-edit elimination.", diffList(new Diff.Change(DELETE, "abxyzcd"), new Diff.Change(INSERT, "12xyz34")), diffs);

        diffs = diffList(new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "x"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "34"));
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: Three-edit elimination.", diffList(new Diff.Change(DELETE, "xcd"), new Diff.Change(INSERT, "12x34")), diffs);

        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "xy"), new Diff.Change(INSERT, "34"), new Diff.Change(EQUAL, "z"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "56"));
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: Backpass elimination.", diffList(new Diff.Change(DELETE, "abxyzcd"), new Diff.Change(INSERT, "12xy34z56")), diffs);

        dmp.getOptions().withEditCost( 5 );
        diffs = diffList(new Diff.Change(DELETE, "ab"), new Diff.Change(INSERT, "12"), new Diff.Change(EQUAL, "wxyz"), new Diff.Change(DELETE, "cd"), new Diff.Change(INSERT, "34"));
        dmp.cleanupEfficiency(diffs);
        Assert.assertEquals("cleanupEfficiency: High cost elimination.", diffList(new Diff.Change(DELETE, "abwxyzcd"), new Diff.Change(INSERT, "12wxyz34")), diffs);
    }

    @Test
    public void testDiffPrettyHtml() {
        // Pretty print.
        dmp = diff(new Diff.Change(EQUAL, "a\n"), new Diff.Change(DELETE, "<B>b</B>"), new Diff.Change(INSERT, "c&d"));
        Assert.assertEquals("prettyHtml:", "<span>a&para;<br></span><del style=\"background:#ffe6e6;\">&lt;B&gt;b&lt;/B&gt;</del><ins style=\"background:#e6ffe6;\">c&amp;d</ins>", dmp.prettyHtml());
    }

    @Test
    public void testDiffText() {
        // Compute the source and destination texts.
        dmp = diff(new Diff.Change(EQUAL, "jump"), new Diff.Change(DELETE, "s"), new Diff.Change(INSERT, "ed"), new Diff.Change(EQUAL, " over "), new Diff.Change(DELETE, "the"), new Diff.Change(INSERT, "a"), new Diff.Change(EQUAL, " lazy"));

        Assert.assertEquals("text1:", "jumps over the lazy", dmp.text1());
        Assert.assertEquals("text2:", "jumped over a lazy", dmp.text2());
    }

    @Test
    public void testDiffDelta() {
        // Convert a diff into delta string.
        dmp = diff(new Diff.Change(EQUAL, "jump"), new Diff.Change(DELETE, "s"), new Diff.Change(INSERT, "ed"), new Diff.Change(EQUAL, " over "), new Diff.Change(DELETE, "the"), new Diff.Change(INSERT, "a"), new Diff.Change(EQUAL, " lazy"), new Diff.Change(INSERT, "old dog"));

        String text1 = dmp.text1();
        Assert.assertEquals("text1: Base text.", "jumps over the lazy", text1);

        String delta = dmp.toDelta();
        Assert.assertEquals("toDelta:", "=4\t-1\t+ed\t=6\t-3\t+a\t=5\t+old dog", delta);

        // Convert delta string into a diff.
        Assert.assertEquals("fromDelta: Normal.", dmp, Diff.fromDelta(text1, delta));

        // Generates error (19 < 20).
        try {
            Diff.fromDelta(text1 + "x", delta);
            Assert.fail("fromDelta: Too long.");
        } catch (IllegalArgumentException ex) {
            // Exception expected.
        }

        // Generates error (19 > 18).
        try {
            Diff.fromDelta(text1.substring(1), delta);
            Assert.fail("fromDelta: Too short.");
        } catch (IllegalArgumentException ex) {
            // Exception expected.
        }

        // Generates error (%c3%xy invalid Unicode).
        try {
            Diff.fromDelta("", "+%c3%xy");
            Assert.fail("fromDelta: Invalid character.");
        } catch (IllegalArgumentException ex) {
            // Exception expected.
        }

        // Test deltas with special characters.
        dmp = diff(new Diff.Change(EQUAL, "\u0680 \000 \t %"), new Diff.Change(DELETE, "\u0681 \001 \n ^"), new Diff.Change(INSERT, "\u0682 \002 \\ |"));
        text1 = dmp.text1();
        Assert.assertEquals("text1: Unicode text.", "\u0680 \000 \t %\u0681 \001 \n ^", text1);

        delta = dmp.toDelta();
        Assert.assertEquals("toDelta: Unicode.", "=7\t-7\t+%DA%82 %02 %5C %7C", delta);

        Assert.assertEquals("fromDelta: Unicode.", dmp, Diff.fromDelta(text1, delta));

        // Verify pool of unchanged characters.
        dmp = diff(new Diff.Change(INSERT, "A-Z a-z 0-9 - _ . ! ~ * ' ( ) ; / ? : @ & = + $ , # "));
        String text2 = dmp.text2();
        Assert.assertEquals("text2: Unchanged characters.", "A-Z a-z 0-9 - _ . ! ~ * \' ( ) ; / ? : @ & = + $ , # ", text2);

        delta = dmp.toDelta();
        Assert.assertEquals("toDelta: Unchanged characters.", "+A-Z a-z 0-9 - _ . ! ~ * \' ( ) ; / ? : @ & = + $ , # ", delta);

        // Convert delta string into a diff.
        Assert.assertEquals("fromDelta: Unchanged characters.", dmp, Diff.fromDelta("", delta));
    }

    @Test
    public void testDiffXIndex() {
        // Translate a location in text1 to text2.
        dmp = diff(new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "1234"), new Diff.Change(EQUAL, "xyz"));
        Assert.assertEquals("xIndex: Translation on equality.", 5, dmp.xIndex(2));

        dmp = diff(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "1234"), new Diff.Change(EQUAL, "xyz"));
        Assert.assertEquals("xIndex: Translation on deletion.", 1, dmp.xIndex(3));
    }

    @Test
    public void testDiffLevenshtein() {
        dmp = diff(new Diff.Change(DELETE, "abc"), new Diff.Change(INSERT, "1234"), new Diff.Change(EQUAL, "xyz"));
        Assert.assertEquals("Levenshtein with trailing equality.", 4, dmp.levenshtein());

        dmp = diff(new Diff.Change(EQUAL, "xyz"), new Diff.Change(DELETE, "abc"), new Diff.Change(INSERT, "1234"));
        Assert.assertEquals("Levenshtein with leading equality.", 4, dmp.levenshtein());

        dmp = diff(new Diff.Change(DELETE, "abc"), new Diff.Change(EQUAL, "xyz"), new Diff.Change(INSERT, "1234"));
        Assert.assertEquals("Levenshtein with middle equality.", 7, dmp.levenshtein());
    }

    @Test
    public void testDiffBisect() {
        // Normal.
        String a = "cat";
        String b = "map";
        // Since the resulting diff hasn't been normalized, it would be ok if
        // the insertion and deletion pairs are swapped.
        // If the order changes, tweak this test as required.
        Diff diffs = diff(new Diff.Change(DELETE, "c"), new Diff.Change(INSERT, "m"), new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "t"), new Diff.Change(INSERT, "p"));
        Assert.assertEquals("bisect: Normal.", diffs, new Diff(dmp.bisect(a, b), dmp.getOptions()));

        dmp.getOptions()
           .withTimeout(0.000000001);
        // timeout.
        diffs = new Diff(diffList(new Diff.Change(DELETE, "cat"), new Diff.Change(INSERT, "map")), dmp.getOptions(), 0);

        Assert.assertEquals("bisect: Timeout.", diffs, new Diff(diffs.bisect(a, b), dmp.getOptions(), 0));
    }

    @Test
    public void testDiffMain() {
        Diff.Options opts = Diff.Options.defaults().withChangeLines(false);

        // Perform a trivial diff.
        Diff diffs = diff();
        Assert.assertEquals("main: Null case.", diffs, new Diff("", "", opts));

        diffs = diff(new Diff.Change(EQUAL, "abc"));
        Assert.assertEquals("main: Equality.", diffs, new Diff("abc", "abc", opts));

        diffs = diff(new Diff.Change(EQUAL, "ab"), new Diff.Change(INSERT, "123"), new Diff.Change(EQUAL, "c"));
        Assert.assertEquals("main: Simple insertion.", diffs, new Diff("abc", "ab123c", opts));

        diffs = diff(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "123"), new Diff.Change(EQUAL, "bc"));
        Assert.assertEquals("main: Simple deletion.", diffs, new Diff("a123bc", "abc", opts));

        diffs = diff(new Diff.Change(EQUAL, "a"), new Diff.Change(INSERT, "123"), new Diff.Change(EQUAL, "b"), new Diff.Change(INSERT, "456"), new Diff.Change(EQUAL, "c"));
        Assert.assertEquals("main: Two insertions.", diffs, new Diff("abc", "a123b456c", opts));

        diffs = diff(new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "123"), new Diff.Change(EQUAL, "b"), new Diff.Change(DELETE, "456"), new Diff.Change(EQUAL, "c"));
        Assert.assertEquals("main: Two deletions.", diffs, new Diff("a123b456c", "abc", opts));

        // Perform a real diff.
        // Switch off the timeout.
        opts = opts.withTimeout(0);
        diffs = diff(new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "b"));
        Assert.assertEquals("main: Simple case #1.", diffs, new Diff("a", "b", opts));

        diffs = diff(new Diff.Change(DELETE, "Apple"), new Diff.Change(INSERT, "Banana"), new Diff.Change(EQUAL, "s are a"), new Diff.Change(INSERT, "lso"), new Diff.Change(EQUAL, " fruit."));
        Assert.assertEquals("main: Simple case #2.", diffs, new Diff("Apples are a fruit.", "Bananas are also fruit.", opts));

        diffs = diff(new Diff.Change(DELETE, "a"), new Diff.Change(INSERT, "\u0680"), new Diff.Change(EQUAL, "x"), new Diff.Change(DELETE, "\t"), new Diff.Change(INSERT, "\000"));
        Assert.assertEquals("main: Simple case #3.", diffs, new Diff("ax\t", "\u0680x\000", opts));

        diffs = diff(new Diff.Change(DELETE, "1"), new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "y"), new Diff.Change(EQUAL, "b"), new Diff.Change(DELETE, "2"), new Diff.Change(INSERT, "xab"));
        Assert.assertEquals("main: Overlap #1.", diffs, new Diff("1ayb2", "abxab", opts));

        diffs = diff(new Diff.Change(INSERT, "xaxcx"), new Diff.Change(EQUAL, "abc"), new Diff.Change(DELETE, "y"));
        Assert.assertEquals("main: Overlap #2.", diffs, new Diff("abcy", "xaxcxabc", opts));

        diffs = diff(new Diff.Change(DELETE, "ABCD"), new Diff.Change(EQUAL, "a"), new Diff.Change(DELETE, "="), new Diff.Change(INSERT, "-"), new Diff.Change(EQUAL, "bcd"), new Diff.Change(DELETE, "="), new Diff.Change(INSERT, "-"), new Diff.Change(EQUAL, "efghijklmnopqrs"), new Diff.Change(DELETE, "EFGHIJKLMNOefg"));
        Assert.assertEquals("main: Overlap #3.", diffs, new Diff("ABCDa=bcd=efghijklmnopqrsEFGHIJKLMNOefg", "a-bcd-efghijklmnopqrs", opts));

        diffs = diff(new Diff.Change(INSERT, " "), new Diff.Change(EQUAL, "a"), new Diff.Change(INSERT, "nd"), new Diff.Change(EQUAL, " [[Pennsylvania]]"), new Diff.Change(DELETE, " and [[New"));
        Assert.assertEquals("main: Large equality.", diffs, new Diff("a [[Pennsylvania]] and [[New", " and [[Pennsylvania]]", opts));

        opts = opts.withTimeout(0.1);  // 100 ms

        String a = "`Twas brillig, and the slithy toves\nDid gyre and gimble in the wabe:\nAll mimsy were the borogoves,\nAnd the mome raths outgrabe.\n";
        String b = "I am the very model of a modern major general,\nI've information vegetable, animal, and mineral,\nI know the kings of England, and I quote the fights historical,\nFrom Marathon to Waterloo, in order categorical.\n";
        // Increase the text lengths by 1024 times to ensure a timeout.
        for (int x = 0; x < 10; x++) {
            a = a + a;
            b = b + b;
        }
        long startTime = opts.getClock().millis();
        new Diff(a, b, opts);
        long endTime = opts.getClock().millis();
        // Test that we took at least the timeout period.
        Assert.assertTrue("main: Timeout min.", opts.getTimeout() * 1000 <= endTime - startTime);
        // Test that we didn't take forever (be forgiving).
        // Theoretically this test could fail very occasionally if the
        // OS task swaps or locks up for a second at the wrong moment.
        Assert.assertTrue("main: Timeout max.", opts.getTimeout() * 1000 * 2 > endTime - startTime);

        opts = opts.withTimeout(0)
                   .withChangeLines(false);

        // Test the linemode speedup.
        // Must be long to pass the 100 char cutoff.
        a = "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n";
        b = "abcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\n";
        Assert.assertEquals("main: Simple line-mode.", new Diff(a, b, opts), new Diff(a, b, opts));

        opts.withChangeLines(true);

        a = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
        b = "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij";
        Assert.assertEquals("main: Single line-mode.", new Diff(a, b, opts), new Diff(a, b, opts));

        a = "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n";
        b = "abcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n";
        String[] texts_linemode = rebuildtexts(new Diff(a, b, opts.withChangeLines(true)));
        String[] texts_textmode = rebuildtexts(new Diff(a, b, opts.withChangeLines(false)));
        assertArrayEquals("main: Overlap line-mode.", texts_textmode, texts_linemode);

        // Test null inputs.
        try {
            new Diff((String) null, (String) null);
            Assert.fail("main: Null inputs.");
        } catch (IllegalArgumentException ex) {
            // Error expected.
        }
    }

    private void assertArrayEquals(String error_msg, Object[] a, Object[] b) {
        List<Object> list_a = Arrays.asList(a);
        List<Object> list_b = Arrays.asList(b);
        Assert.assertEquals(error_msg, list_a, list_b);
    }

    private void assertLinesToCharsResultEquals(String error_msg,
                                                Diff.LinesToCharsResult a, Diff.LinesToCharsResult b) {
        Assert.assertEquals(error_msg, a.chars1, b.chars1);
        Assert.assertEquals(error_msg, a.chars2, b.chars2);
        Assert.assertEquals(error_msg, a.lineArray, b.lineArray);
    }

    // Construct the two texts which made up the diff originally.
    private static String[] rebuildtexts(Diff diffs) {
        String[] text = {"", ""};
        for (Diff.Change myDiff : diffs.getChangeList()) {
            if (myDiff.operation != Diff.Operation.INSERT) {
                text[0] += myDiff.text;
            }
            if (myDiff.operation != Diff.Operation.DELETE) {
                text[1] += myDiff.text;
            }
        }
        return text;
    }

    // Private function for quickly building lists of diffs.
    private static LinkedList<Diff.Change> diffList(Diff.Change... diffs) {
        LinkedList<Diff.Change> myDiffList = new LinkedList<Diff.Change>();
        for (Diff.Change myDiff : diffs) {
            myDiffList.add(myDiff);
        }
        return myDiffList;
    }

    private static Diff diff(Diff.Change... diffs) {
        return new Diff(diffList(diffs), Diff.Options.defaults());
    }
}
