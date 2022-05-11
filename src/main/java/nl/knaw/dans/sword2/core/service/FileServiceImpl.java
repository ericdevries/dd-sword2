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

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class FileServiceImpl implements FileService {
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public void ensureDirectoriesExist(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    @Override
    public long getFileSize(Path path) {
        return 0;
    }

    @Override
    public Path copyFile(InputStream inputStream, Path target) throws IOException {
        ensureDirectoriesExist(target.getParent());
        Files.copy(inputStream, target);
        return target;
    }

    @Override
    public String copyFileWithMD5Hash(InputStream inputStream, Path target) throws IOException {
        try {
            var digest = MessageDigest.getInstance("MD5");
            var stream = new DigestInputStream(inputStream, digest);

            copyFile(stream, target);

            return DatatypeConverter.printHexBinary(digest.digest())
                .toLowerCase(Locale.ROOT);

        } catch (NoSuchAlgorithmException e) {
            // noop
        }

        throw new IOException(String.format("Unable to copy file to target %s", target));
    }

    @Override
    public Path copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target);
        return target;
    }

    @Override
    public long getAvailableDiskSpace(Path path) throws IOException {
        var fileStore = Files.getFileStore(path);
        return fileStore.getUsableSpace();
    }

    @Override
    public Stream<Path> listFiles(Path path) throws IOException {
        return Files.list(path).filter(Files::isRegularFile);
    }

    @Override
    public List<Path> listDirectories(Path path) throws IOException {
        return Files.list(path).filter(Files::isDirectory).collect(Collectors.toList());
    }

    @Override
    public void deleteFile(Path file) throws IOException {
        Files.deleteIfExists(file);
    }

    @Override
    public void move(Path sourcePath, Path targetPath) throws IOException {
        ensureDirectoriesExist(targetPath.getParent());
        Files.move(sourcePath, targetPath);
    }

    @Override
    public Path mergeFiles(List<Path> files, Path target) throws IOException {
        try (var output = new BufferedOutputStream(new FileOutputStream(target.toFile(), true))) {
            for (var file: files) {
                IOUtils.copy(Files.newInputStream(file), output);
            }
        } finally {
            for (var file: files) {
                Files.deleteIfExists(file);
            }
        }

        return target;
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public Path writeContentToFile(Path path, String content) throws IOException {
        return Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String readFile(Path item) throws IOException {
        return Files.readString(item);
    }

    @Override
    public List<String> readLines(Path file) throws IOException {
        return Files.readAllLines(file);
    }

}
