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
import nl.knaw.dans.sword2.exceptions.InvalidPartialFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class BagExtractorImpl implements BagExtractor {

    private static final Logger log = LoggerFactory.getLogger(BagExtractorImpl.class);
    private final Pattern defaultPrefixPattern = Pattern.compile("^[^/]+/data/");
    private final ZipService zipService;
    private final FileService fileService;
    private final BagItManager bagItManager;

    @Inject
    public BagExtractorImpl(ZipService zipService, FileService fileService, BagItManager bagItManager) {
        this.zipService = zipService;
        this.fileService = fileService;
        this.bagItManager = bagItManager;
    }

    @Override
    public void extractBag(Path path, String mimeType, boolean filePathMapping) throws Exception, InvalidDepositException, InvalidPartialFileException {
        log.debug("Extracting bag {} with mimeType {} and file path mapping set to {}", path, mimeType, filePathMapping);

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

    void extractOctetStream(Path path, boolean filePathMapping) throws Exception, InvalidPartialFileException, InvalidDepositException {
        var files = getDepositFiles(path);
        var sorting = new HashMap<Path, Integer>();

        for (var file : files) {
            var sequenceNumber = getSequenceNumber(file);
            log.trace("Sequence number for file {}: {}", file, sequenceNumber);
            sorting.put(file, sequenceNumber);
        }

        files.sort(Comparator.comparing(sorting::get));

        var output = path.resolve("merged.zip");
        fileService.mergeFiles(files, output);

        log.debug("Extracting merged zip in path {}", path);
        extractZips(path, filePathMapping);
    }

    int getSequenceNumber(Path path) throws Exception, InvalidPartialFileException {
        var parts = path.getFileName().toString().split("\\.");

        if (parts.length <= 1) {
            throw new InvalidPartialFileException(String.format("Partial file %s has no extension. It should be a positive sequence number.", path));
        }

        try {
            var value = Integer.parseInt(parts[parts.length - 1], 10);

            if (value <= 0) {
                throw new InvalidPartialFileException(String.format("Partial file %s has an incorrect extension. It should be a positive sequence number (> 0), but was: %s", path, value));
            }

            return value;
        }
        catch (NumberFormatException e) {
            throw new InvalidPartialFileException(String.format("Partial file %s has an incorrect extension. Should be a positive sequence number.", path));
        }
    }

    void extractZips(Path path, boolean filePathMapping) throws IOException, InvalidDepositException {
        var files = getDepositFiles(path);

        for (var zipFile : files) {
            extract(zipFile, path, filePathMapping);
        }
    }

    List<Path> getDepositFiles(Path path) throws IOException {
        return fileService.listFiles(path).filter(f -> !f.getFileName().equals(Path.of("deposit.properties"))).collect(Collectors.toList());
    }

    void extract(Path zipFile, Path target, boolean filePathMapping) throws IOException, InvalidDepositException {
        if (filePathMapping) {
            extractWithFilePathMapping(zipFile, target, generateFilePathMapping(zipFile));
        }
        else {
            extractWithFilePathMapping(zipFile, target, Map.of());
        }
    }

    void extractWithFilePathMapping(Path zipFile, Path target, Map<String, String> filePathMapping) throws IOException, InvalidDepositException {
        fileService.ensureDirectoriesExist(target);

        log.debug("Extracting file {} to target {} with file path mapping set to {}", zipFile, target, filePathMapping);
        zipService.extractZipFileWithFileMapping(zipFile, target, filePathMapping);

        bagItManager.updateManifests(target, filePathMapping);
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

    @Override
    public Path getBagDir(Path path) throws IOException, InvalidDepositException {
        var files = fileService.listDirectories(path);

        if (files.size() != 1) {
            throw new InvalidDepositException(String.format("A deposit package must contain exactly one top-level directory, number found: %s", files.size()));
        }

        return files.get(0);
    }
}
