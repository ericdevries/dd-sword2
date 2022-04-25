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

import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import org.w3c.dom.stylesheets.LinkStyle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class BagExtractorImpl implements BagExtractor {
    private final Pattern defaultPrefixPattern = Pattern.compile("^[^/]+/data/");
    private final ZipService zipService;
    private final FileService fileService;

    @Inject
    public BagExtractorImpl(ZipService zipService, FileService fileService) {
        this.zipService = zipService;
        this.fileService = fileService;
    }

    @Override
    public void extractBag(Path path, String mimeType, boolean filePathMapping) throws Exception, InvalidDepositException {
        switch (mimeType) {
            case "application/zip":
                extractZips(path, filePathMapping);
                break;

            case "application/octet-stream":
                extractOctetStream(path, filePathMapping);
                break;

            default:
                throw new InvalidDepositException(String.format("Unknown mime-type %s", mimeType));
        }
    }

    void extractOctetStream(Path path, boolean filePathMapping) throws Exception {
        var files = getDepositFiles(path);

        // for validation
        for (var file: files) {
            getSequenceNumber(file);
        }

        files.sort((left, right) -> {
            try {
                return getSequenceNumber(left) - getSequenceNumber(right);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        });

        var output = path.resolve("merged.zip");
        fileService.mergeFiles(files, output);

        extractZips(path, filePathMapping);
    }

    int getSequenceNumber(Path path) throws Exception {
        var parts = path.getFileName().toString().split("\\.");

        if (parts.length <= 1) {
            throw new Exception(String.format("Invalid partial file name: %s", path));
        }

        return Integer.parseInt(parts[parts.length - 1], 10);
    }

    void extractZips(Path path, boolean filePathMapping) throws IOException {
        var files = getDepositFiles(path);

        for (var zipFile : files) {
            extract(zipFile, path, filePathMapping);
        }
    }

    List<Path> getDepositFiles(Path path) throws IOException {
        return fileService.listFiles(path).filter(f -> !f.getFileName().equals(Path.of("deposit.properties")))
            .collect(Collectors.toList());
    }

    void extract(Path zipFile, Path target, boolean filePathMapping) throws IOException {
        if (filePathMapping) {
            extractWithFilePathMapping(zipFile, target, generateFilePathMapping(zipFile));
        }
        else {
            extractWithFilePathMapping(zipFile, target, Map.of());
        }
    }

    void extractWithFilePathMapping(Path zipFile, Path target, Map<String, String> filePathMapping) throws IOException {
        fileService.ensureDirectoriesExist(target);
        zipService.extractZipFileWithFileMapping(zipFile, target, filePathMapping);
    }

    Map<String, String> generateFilePathMapping(Path zipFile) throws IOException {
        return generateFilePathMapping(zipFile, defaultPrefixPattern);
    }

    Map<String, String> generateFilePathMapping(Path zipFile, Pattern prefixPattern) throws IOException {
        var fileNames = zipService.getFilesInZip(zipFile);

        return fileNames.stream().map(fileName -> {
            var matcher = prefixPattern.matcher(fileName);

            if (matcher.find()) {
                var prefix = matcher.group();
                var newPath = Path.of(prefix, UUID.randomUUID().toString()).toString();

                return Map.entry(fileName, newPath);
            }

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
