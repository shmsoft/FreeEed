/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.util;

import de.schlichtherle.truezip.file.TFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * @author mark
 */
public class TrueZipUtil {

    private static final Logger logger = LoggerFactory.getLogger(TrueZipUtil.class);
    private int count;

    public int countFiles(String zipFilePath) {
        try {
            count = 0;
            countArchivesRecursively(new TFile(zipFilePath));
        } catch (IOException | InterruptedException e) {
            logger.error("Problem counting files in {}", zipFilePath);
        }
        return count;
    }

    private void countArchivesRecursively(TFile tfile)
            throws IOException, InterruptedException {
        if ((tfile.isDirectory() || tfile.isArchive())) {
            TFile[] files = tfile.listFiles();
            if (files != null) {
                for (TFile file : files) {
                    countArchivesRecursively(file);
                }
            }
        } else {
            ++count;
        }
    }

    public static void mergeTwoZips(String parent, String child) throws IOException {
        Path parentPath = Paths.get(parent);
        Path childPath = Paths.get(child);
        try (FileSystem parentFS = FileSystems.newFileSystem(parentPath, TrueZipUtil.class.getClassLoader())) {
            try (FileSystem childFS = FileSystems.newFileSystem(childPath, TrueZipUtil.class.getClassLoader())) {
                traverseAndCopy(parentFS, childFS);
            }
        }
    }

    private static void traverseAndCopy(final FileSystem parentFS, FileSystem childFS) throws IOException {
        final Path root = childFS.getPath("/");
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                copyFile(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                copyFile(dir);
                return FileVisitResult.CONTINUE;
            }

            private void copyFile(Path path) throws IOException {
                if (Objects.nonNull(path) && !"/".equals(path.toString().trim())) {
                    System.out.println("copying file " + path + " to parent");
                    Files.copy(path, parentFS.getPath(path.toString()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        });
    }
}
