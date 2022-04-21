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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
    public Path copyFile(Path source, Path target) throws IOException {
        Files.copy(source, target);
        return target;
    }

    @Override
    public long getAvailableDiskSpace(Path path) throws IOException {
        var fileStore = Files.getFileStore(path);
        return fileStore.getUsableSpace();
    }

}
