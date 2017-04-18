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

import net.morimekta.util.concurrent.ProcessExecutor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * Helper to handle process forking related to testing a jar file.
 */
public class IntegrationExecutor {
    private final File      jarFile;
    private Long            deadlineMs;
    private InputStream     inputStream;
    private ProcessExecutor executor;

    public IntegrationExecutor(String module, String name) throws IOException {
        this(findMavenTargetJar(module, name));
    }

    protected IntegrationExecutor(File jarFile) {
        this.jarFile = jarFile;
    }

    /**
     * @return The programs stdout content.
     */
    public String getOutput() {
        if (executor == null) return "";
        return executor.getOutput();
    }

    /**
     * @return The programs stderr content.
     */
    public String getError() {
        if (executor == null) return "";
        return executor.getError();
    }

    /**
     * Set input stream to write to the process as program input.
     *
     * @param in The program input.
     */
    public void setInput(InputStream in) {
        this.inputStream = in;
    }

    /**
     * Set the program deadline. If not finished in this time interval,
     * the run fails with an IOException.
     *
     * @param deadlineMs The new deadline in milliseconds. 0 means to wait
     *                   forever. Default is 1 second.
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
            String[] cmd = new String[args.length + 3];
            cmd[0] = "java";
            cmd[1] = "-jar";
            cmd[2] = jarFile.getCanonicalPath();
            if (args.length > 0) {
                System.arraycopy(args, 0, cmd, 3, args.length);
            }

            executor = new ProcessExecutor(cmd);
            if (inputStream != null) {
                executor.setInput(inputStream);
            }
            if (deadlineMs != null) {
                executor.setDeadlineMs(deadlineMs);
            }
            return executor.call();
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private static File findMavenTargetJar(String module, String name) throws IOException {
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
}
