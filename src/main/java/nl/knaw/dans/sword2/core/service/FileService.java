/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
package nl.knaw.dans.sword2.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface FileService {

    void ensureDirectoriesExist(Path directory) throws IOException;

    Path copyFile(InputStream inputStream, Path target) throws IOException;

    String copyFileWithMD5Hash(InputStream inputStream, Path target) throws IOException;

    Path copyFile(Path source, Path target) throws IOException;

    long getAvailableDiskSpace(Path path) throws IOException;

    Stream<Path> listFiles(Path path) throws IOException;

    List<Path> listDirectories(Path path) throws IOException;

    void deleteFile(Path file) throws IOException;

    void move(Path sourcePath, Path targetPath) throws IOException;

    Path mergeFiles(List<Path> files, Path target) throws IOException;

    boolean exists(Path path);

    Path writeContentToFile(Path path, String content) throws IOException;

    List<String> readLines(Path file) throws IOException;

    void deleteDirectory(Path directory) throws IOException;

    boolean isSameFileSystem(Path ...paths) throws IOException;

    boolean canWriteTo(Path path);
}
