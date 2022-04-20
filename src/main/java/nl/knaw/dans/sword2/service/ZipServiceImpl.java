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
package nl.knaw.dans.sword2.service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Singleton
public class ZipServiceImpl implements ZipService {
    private final FileService fileService;

    @Inject
    public ZipServiceImpl(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void extractZipFile(Path path, Path targetPath) throws IOException {
        extractZipFileWithFileMapping(path, targetPath, Map.of());
    }

    @Override
    public void extractZipFileWithFileMapping(Path path, Path targetPath, Map<String, String> fileMapping) throws IOException {
        var file = new ZipFile(path.toFile());
        extractZipFileWithFileMapping(file, targetPath, fileMapping);
    }

    void extractZipFileWithFileMapping(ZipFile zipFile, Path targetPath, Map<String, String> fileMapping) throws IOException {

        zipFile.stream().filter(e -> !e.getName().endsWith("/")).forEach(entry -> {
            var name = entry.getName();
            var target = targetPath.resolve(Path.of(fileMapping.getOrDefault(name, name)));

            try {
                fileService.copyFile(zipFile.getInputStream(entry), target);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<String> getFilesInZip(Path path) throws IOException {
        return getFilesInZip(new ZipFile(path.toFile()));
    }

    List<String> getFilesInZip(ZipFile zipFile) {
        return zipFile.stream().map(ZipEntry::getName).filter(e -> !e.endsWith("/")).collect(Collectors.toList());
    }
}