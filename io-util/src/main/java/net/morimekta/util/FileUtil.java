/*
 * Copyright (c) 2018, Stein Eldar Johnsen
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
package net.morimekta.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Random;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * NIO file utility extensions.
 */
public class FileUtil {
    /**
     * Read and parse the path to its absolute canonical path.
     * <p>
     * To circumvent the problem that java cached file metadata, including symlink
     * targets, we need to read canonical paths directly. This includes resolving
     * symlinks and relative path resolution (../..).<br>
     * <p>
     * This will read all the file meta each time and not use any of the java file
     * meta caching, so will probably be a little slower. So should not be used
     * repeatedly or too often.
     *
     * @param path The path to make canonical path of.
     * @return The resolved canonical path.
     * @throws IOException If unable to read the path.
     */
    public static Path readCanonicalPath(Path path) throws IOException {
        if (!path.isAbsolute()) {
            path = path.toAbsolutePath();
        }
        if (path.toString().equals(File.separator)) {
            return path;
        }

        String fileName = path.getFileName().toString();
        if (".".equals(fileName)) {
            path = path.getParent();
            fileName = path.getFileName().toString();
        }

        // resolve ".." relative to the top of the path.
        int parents = 0;
        while ("..".equals(fileName)) {
            path = path.getParent();
            if (path == null || path.getFileName() == null) {
                throw new IOException("Parent of root does not exist!");
            }
            fileName = path.getFileName().toString();
            ++parents;
        }
        while (parents-- > 0) {
            path = path.getParent();
            if (path == null || path.getFileName() == null) {
                throw new IOException("Parent of root does not exist!");
            }
            fileName = path.getFileName().toString();
        }

        if (path.getParent() != null) {
            Path parent = readCanonicalPath(path.getParent());
            path = parent.resolve(fileName);

            if (Files.isSymbolicLink(path)) {
                path = Files.readSymbolicLink(path);
                if (!path.isAbsolute()) {
                    path = readCanonicalPath(parent.resolve(path));
                }
            }
        }

        return path;
    }

    /**
     * Similar to {@link Files#createSymbolicLink(Path, Path, FileAttribute[])}, but
     * will replace the link if it already exists, and will try to write / replace
     * it as an atomic operation.
     *
     * @param link The path of the symbolic link. The parent directory of the
     *             link file must already exist, and must be writable.
     *             See {@link Files#createDirectories(Path, FileAttribute[])}
     * @param target The target path.
     * @throws IOException If unable to create symbolic link.
     */
    public static void replaceSymbolicLink(Path link, Path target) throws IOException {
        link = link.toAbsolutePath();
        if (Files.exists(link, NOFOLLOW_LINKS)) {
            if (!Files.isSymbolicLink(link)) {
                throw new NotLinkException(String.format("%s is not a symbolic link", link));
            }
            // This operation will follow the link. And we want it to.
            if (Files.isDirectory(link)) {
                // TODO: Figure out how to atomically replace link to directory.
                // Java complains about target being a directory, as it seems to
                // resolve the old link and trying to move onto that target, and
                // it won't replace a directory with a link.
                // And that is despite the NOFOLLOW_LINKS option.
                Files.delete(link);
                Files.createSymbolicLink(link, target);
            } else {
                Path parent = readCanonicalPath(link.getParent());
                Path temp   = parent.resolve(String.format("..tmp.%x", new Random().nextLong()));
                try {
                    Files.createSymbolicLink(temp, target);
                    Files.move(temp, link, ATOMIC_MOVE, REPLACE_EXISTING, NOFOLLOW_LINKS);
                } finally {
                    Files.deleteIfExists(temp);
                }
            }
        } else {
            Files.createSymbolicLink(link, target);
        }
    }

    private FileUtil() {}
}
