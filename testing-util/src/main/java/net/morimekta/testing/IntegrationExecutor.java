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

import net.morimekta.util.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Helper to handle process forking related to testing a jar file.
 */
public class IntegrationExecutor {
    private final File jarFile;
    private final Runtime runtime;
    private ByteArrayOutputStream out;
    private ByteArrayOutputStream err;
    private InputStream in;

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
        this.in = new ByteArrayInputStream(new byte[0]);
    }

    public String getOutput() {
        return new String(out.toByteArray(), UTF_8);
    }

    public String getError() {
        return new String(err.toByteArray(), UTF_8);
    }

    public void setInput(InputStream in) {
        this.in = in;
    }

    public int run() throws IOException {
        return run(new String[]{});
    }

    public int run(String[] args) throws IOException {
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

            IOUtils.copy(in, process.getOutputStream());

            process.waitFor();

            IOUtils.copy(process.getInputStream(), out);
            IOUtils.copy(process.getErrorStream(), err);

            return process.exitValue();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
