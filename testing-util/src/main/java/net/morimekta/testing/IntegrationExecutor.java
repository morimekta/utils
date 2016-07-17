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
package net.morimekta.testing;

import net.morimekta.util.Strings;
import net.morimekta.util.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Helper to handle process forking related to testing a jar file.
 */
public class IntegrationExecutor {
    private final File            jarFile;
    private final Runtime         runtime;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private InputStream           in;
    private long                  deadlineMs;

    public static File findMavenTargetJar(String module, String name) throws IOException {
        if (new File(module).isDirectory()) {
            File inModule = new File(module + "/target/" + name);

            if (inModule.isFile()) {
                return inModule;
            }
        }

        File local = new File("target/" + name);
        if (local.isFile()) {
            return local;
        }

        throw new IOException("No such jar file: " + name);
    }

    public IntegrationExecutor(String module, String name) throws IOException {
        this(module, name, Runtime.getRuntime());
    }

    private IntegrationExecutor(String module, String name, Runtime runtime) throws IOException {
        this(findMavenTargetJar(module, name), runtime);
    }

    public IntegrationExecutor(File jarFile) {
        this(jarFile, Runtime.getRuntime());
    }

    private IntegrationExecutor(File jarFile, Runtime runtime) {
        this.jarFile = jarFile;
        this.runtime = runtime;

        this.out = new ByteArrayOutputStream();
        this.err = new ByteArrayOutputStream();
        this.in = null;
        this.deadlineMs = 60L * 1000L;
    }

    /**
     * @return The programs stdout content.
     */
    public String getOutput() {
        return new String(out.toByteArray(), UTF_8);
    }

    /**
     * @return The programs stderr content.
     */
    public String getError() {
        return new String(err.toByteArray(), UTF_8);
    }

    /**
     * Set input stream to write to the process as program input.
     *
     * @param in The program input.
     */
    public void setInput(InputStream in) {
        this.in = in;
    }

    /**
     * Set the program deadline. If not finished in this time interval,
     * the run fails with an IOException.
     *
     * @param deadlineMs The new deadline in milliseconds. 0 means to wait
     *                   forever. Default is 60 seconds.
     */
    public void setDeadlineMs(long deadlineMs) {
        this.deadlineMs = deadlineMs;
    }

    /**
     * Run the program with the specified arguments.
     *
     * @param args The arguments.
     * @return The programs exit code.
     * @throws IOException If the run times out or is interrupted.
     */
    public int run(String... args) throws IOException {
        try {
            out.reset();
            err.reset();

            String[] cmd = new String[args.length + 3];
            cmd[0] = "java";
            cmd[1] = "-jar";
            cmd[2] = jarFile.getCanonicalPath();
            if (args.length > 0) {
                System.arraycopy(args, 0, cmd, 3, args.length);
            }
            Process process = runtime.exec(cmd);

            if (in != null) {
                IOUtils.copy(in, process.getOutputStream());
            }
            process.getOutputStream().close();

            if (deadlineMs > 0) {
                if (!process.waitFor(deadlineMs, TimeUnit.MILLISECONDS)) {
                    process.destroyForcibly();

                    StringBuilder bld = new StringBuilder();
                    boolean first = true;
                    for (String c : cmd) {
                        if (first) {
                            first = false;
                        } else {
                            bld.append(" ");
                        }

                        String esc = Strings.escape(c);

                        // Quote where escaping is needed, OR the argument
                        // contains a literal space.
                        if (!c.equals(esc) || c.contains(" ")) {
                            bld.append('\"')
                               .append(esc)
                               .append('\"');
                        } else {
                            bld.append(c);
                        }
                    }

                    throw new IOException("Process took too long: " + bld.toString());
                }
            } else {
                process.waitFor();
            }

            IOUtils.copy(process.getInputStream(), out);
            IOUtils.copy(process.getErrorStream(), err);

            return process.exitValue();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
