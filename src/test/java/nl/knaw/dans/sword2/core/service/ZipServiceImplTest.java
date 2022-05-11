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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ZipServiceImplTest {

    @Test
    void getFilesInZip() {
        var file = Mockito.mock(ZipFile.class);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(file.stream()).thenAnswer(i -> Stream.of(
            new ZipEntry("file1"),
            new ZipEntry("file2.txt"),
            new ZipEntry("path/to/file3.tt"),
            new ZipEntry("folder/"),
            new ZipEntry("folder with spaces/file with spaces.txt")));

        var result = new ZipServiceImpl(fileService).getFilesInZip(file);

        assertEquals(4, result.size());

        assertThat(result).containsOnly("file1", "file2.txt", "path/to/file3.tt", "folder with spaces/file with spaces.txt");
    }

    @Test
    void testAllFilesAreExtractedWithoutFileMapping() throws IOException {
        var file = Mockito.mock(ZipFile.class);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(file.stream()).thenAnswer(i -> Stream.of(
            new ZipEntry("file1"),
            new ZipEntry("file2.txt"),
            new ZipEntry("path/to/file3.tt"),
            new ZipEntry("folder/"),
            new ZipEntry("folder with spaces/file with spaces.txt")));

        var is = new ByteArrayInputStream(new byte[1]);
        Mockito.when(file.getInputStream(Mockito.any())).thenReturn(is);

        new ZipServiceImpl(fileService).extractZipFileWithFileMapping(file, Path.of("target/path"), Map.of());

        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/file1")));
        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/file2.txt")));
        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/path/to/file3.tt")));
        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/folder with spaces/file with spaces.txt")));
    }

    @Test
    void testAllFilesAreExtractedWithFileMapping() throws IOException {
        var file = Mockito.mock(ZipFile.class);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(file.stream()).thenAnswer(i -> Stream.of(
            new ZipEntry("file1"),
            new ZipEntry("path/to/file3.tt"),
            new ZipEntry("folder with spaces/file with spaces.txt")));

        var is = new ByteArrayInputStream(new byte[1]);
        Mockito.when(file.getInputStream(Mockito.any())).thenReturn(is);

        var fileMapping = Map.of("file1", "some-other-value", "path/to/file3.tt", "secret-file");
        new ZipServiceImpl(fileService).extractZipFileWithFileMapping(file, Path.of("target/path"), fileMapping);


        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/some-other-value")));
        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/secret-file")));
        Mockito.verify(fileService)
            .copyFile(Mockito.eq(is), Mockito.eq(Path.of("target/path/folder with spaces/file with spaces.txt")));
    }
}
