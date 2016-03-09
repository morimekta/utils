/*
 * Copyright (c) 2016, Stein Eldar Johnsen
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.morimekta.util.io;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Stack;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Print writer that prints lines with indent. In order to have full control
 * over the lines line-separators will be pre-pended the println calls, not
 * appended. This is a break with the standard convention, but then the calls
 * to println, appendln etc will always be consistent with the next
 * line-length.
 * <p/>
 * Example usage:
 * <pre>
 *     IndentedPrintWriter pw = new IndentedPrintWriter(out);
 *     pw.appendln("Start:");
 *     pw.begin();
 *     pw.println(144);
 *     pw.println(12.77d);
 *     pw.end();
 *     pw.appendln("end.");
 * </pre>
 * Will print the following:
 * <pre>
 * Start:
 *     144
 *     12.77
 * end.
 * </pre>
 */
public class IndentedPrintWriter extends PrintWriter {
    public final static String NEWLINE = "\n";
    public final static String INDENT  = "    ";

    private final Stack<String> indents;
    private final String        indent;
    private final String        newline;
    private       String        current;

    public IndentedPrintWriter(OutputStream out) {
        this(out, INDENT, NEWLINE);
    }

    public IndentedPrintWriter(OutputStream out, String indent, String newline) {
        this(new OutputStreamWriter(out, UTF_8), indent, newline);
    }

    public IndentedPrintWriter(Writer out) {
        this(out, INDENT, NEWLINE);
    }

    public IndentedPrintWriter(Writer out, String indent, String newline) {
        super(out);
        this.indent = indent;
        this.newline = newline;

        this.indents = new Stack<>();
        this.current = "";
    }

    public IndentedPrintWriter begin() {
        return begin(indent);
    }

    public IndentedPrintWriter begin(String indent) {
        indents.push(current);
        current = current + indent;
        return this;
    }

    public IndentedPrintWriter end() {
        if (indents.isEmpty()) {
            throw new IllegalStateException("No indent to end");
        }
        current = indents.pop();
        return this;
    }

    public IndentedPrintWriter newline() {
        return append(newline);
    }

    public IndentedPrintWriter appendln() {
        newline();
        super.append(current);
        return this;
    }

    public IndentedPrintWriter appendln(char c) {
        appendln();
        super.append(c);
        return this;
    }

    public IndentedPrintWriter appendln(CharSequence str) {
        appendln();
        super.append(str);
        return this;
    }

    public IndentedPrintWriter formatln(String format, Object... args) {
        appendln();
        super.format(format, args);
        return this;
    }

    // --- Override PrintWriter methods to return IndentedPrintWriter.

    @Override
    public IndentedPrintWriter printf(String format, Object... args) {
        super.format(format, args);
        return this;
    }

    @Override
    public IndentedPrintWriter printf(Locale l, String format, Object... args) {
        super.format(l, format, args);
        return this;
    }

    @Override
    public IndentedPrintWriter format(String format, Object... args) {
        super.format(format, args);
        return this;
    }

    @Override
    public IndentedPrintWriter format(Locale l, String format, Object... args) {
        super.format(l, format, args);
        return this;
    }

    @Override
    public IndentedPrintWriter append(CharSequence str) {
        super.append(str);
        return this;
    }

    @Override
    public IndentedPrintWriter append(CharSequence str, int start, int end) {
        super.append(str, start, end);
        return this;
    }

    @Override
    public IndentedPrintWriter append(char c) {
        super.append(c);
        return this;
    }

    // --- Override PrintWriter methods to work like IndentedPrintWriter.

    @Override
    public void println() {
        newline();
    }

    @Override
    public void println(boolean x) {
        appendln().print(x);
    }

    @Override
    public void println(char x) {
        appendln().print(x);
    }

    @Override
    public void println(int x) {
        appendln().print(x);
    }

    @Override
    public void println(long x) {
        appendln().print(x);
    }

    @Override
    public void println(float x) {
        appendln().print(x);
    }

    @Override
    public void println(double x) {
        appendln().print(x);
    }

    @Override
    public void println(char[] x) {
        appendln().print(x);
    }

    @Override
    public void println(String x) {
        appendln(x);
    }

    @Override
    public void println(Object x) {
        appendln(String.valueOf(x));
    }
}
