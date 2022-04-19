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

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class BagExtractorImpl implements BagExtractor {
    private final Pattern prefixPattern = Pattern.compile("^[^/]+/data/");
    private final ZipService zipService;

    @Inject
    public BagExtractorImpl(ZipService zipService) {
        this.zipService = zipService;
    }

    @Override
    public void extractBag(Path path, Path target, boolean filePathMapping) {
        try {
            if (filePathMapping) {
                extractZipWithFilePathMapping(path, target);
            }
            else {
                extractZip(path);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    void extractZipWithFilePathMapping(Path path, Path target) throws IOException {
        var fileNames = zipService.getFilesInZip(path);

        var entries = fileNames.stream().map(fileName -> {
            var matcher = prefixPattern.matcher(fileName);
            if (matcher.find()) {
                var prefix = matcher.group();
                var newPath = Path.of(prefix, UUID.randomUUID().toString()).toString();

                return Map.entry(fileName, newPath);
            }

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        zipService.extractZipFileWithFileMapping(path, target, entries);
        ////
        ////        extractZip(path);
        ////
        ////        var file = new ZipFile(path.toString());
        ////        var rootDir = Files.list(path.getParent()).filter(Files::isDirectory).findFirst();
        ////
        ////        try {
        ////            var filelist = file.getFileHeaders().stream().map(item -> {
        ////                    var name = item.getFileName();
        ////                    var matcher = prefixPattern.matcher(name);
        ////
        ////                    if (matcher.find()) {
        ////                        var prefix = matcher.group();
        ////
        ////                        return Path.of(prefix, UUID.randomUUID().toString());
        ////                    }
        ////
        ////                    return null;
        ////                })
        ////                .filter(Objects::nonNull)
        ////                .map(name -> {
        ////                    System.out.println("RELATIVE: " + rootDir.get().relativize(name));
        ////                    return name;
        ////                    /*
        ////
        ////  def toBagRelativeMapping(zipRelativeMapping: Map[String, String], bagName: String): Try[Map[String, String]] = Try {
        ////    zipRelativeMapping.map {
        ////      case (orgName, newName) => (Paths.get(bagName).relativize(Paths.get(orgName)).toString, Paths.get(bagName).relativize(Paths.get(newName)).toString)
        ////    }
        ////  }
        ////                     */
        ////                }).collect(Collectors.toList());
        ////
        ////            System.out.println("FILES: ");
        ////
        ////            for (var item : filelist) {
        ////                System.out.println("- " + item);
        ////            }
        //        }
        //        catch (ZipException e) {
        //            e.printStackTrace();
        //        }
    }

    void extractZip(Path path) {
        var file = new ZipFile(path.toString());

        try {
            file.extractAll(path.getParent().toString());
        }
        catch (ZipException e) {
            e.printStackTrace();
        }
    }
}
