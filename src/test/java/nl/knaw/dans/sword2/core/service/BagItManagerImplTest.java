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

import org.apache.commons.lang3.tuple.Pair;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BagItManagerImplTest {


    @Test
    void isManifestFile() {
        var fileService = Mockito.mock(FileService.class);
        var checksumCalculator = Mockito.mock(ChecksumCalculator.class);
        var bagItManager = new BagItManagerImpl(fileService, checksumCalculator);

        assertTrue(bagItManager.isManifestFile(Path.of("manifest-md5.txt")));
        assertTrue(bagItManager.isManifestFile(Path.of("manifest-sha1.txt")));
        assertTrue(bagItManager.isManifestFile(Path.of("manifest-something.txt")));
        assertTrue(bagItManager.isManifestFile(Path.of("path/to/manifest-md5.txt")));
        assertTrue(bagItManager.isManifestFile(Path.of("path/with spaces/manifest-md5.txt")));

        assertFalse(bagItManager.isManifestFile(Path.of("manifest2-md5.txt")));
        assertFalse(bagItManager.isManifestFile(Path.of("manifest2-sha1.txt")));
        assertFalse(bagItManager.isManifestFile(Path.of("themanifest-something.txt")));
        assertFalse(bagItManager.isManifestFile(Path.of("path/to/manifest-md5/subfolder.txt")));
        assertFalse(bagItManager.isManifestFile(Path.of("path/with spaces/manifest.txt")));
    }

    @Test
    void getManifestFiles() {

    }

    @Test
    void parseManifestFile() throws IOException {
        var fileService = Mockito.mock(FileService.class);
        var checksumCalculator = Mockito.mock(ChecksumCalculator.class);
        var bagItManager = new BagItManagerImpl(fileService, checksumCalculator);

        var fileContent = List.of("x  data/a.txt", "y  data/b.txt", "z  data/c.txt");
        var filePathMapping = new HashMap<String, String>();
        filePathMapping.put("data/a.txt", "data/1");
        filePathMapping.put("data/b.txt", "data/2");
        filePathMapping.put("data/c.txt", "data/3");

        Mockito.when(fileService.readLines(Mockito.any())).thenReturn(fileContent);

        var result = bagItManager.parseManifestFile(
            Path.of("test"), filePathMapping);

        Assertions.assertThat(result).containsOnly(
            Pair.of("x", "data/1"),
            Pair.of("y", "data/2"),
            Pair.of("z", "data/3")
        );
    }
}
