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

import nl.knaw.dans.sword2.core.exceptions.NotEnoughDiskSpaceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FilesystemSpaceVerifierImplTest {

    final static long GB = 1024*1024*1024;
    final static long MB = 1024*1024;


    @Test
    void assertDirHasEnoughDiskSpaceMarginForFile() throws IOException, NotEnoughDiskSpaceException {
        var fileService = Mockito.mock(FileService.class);
        var fileSize = MB*5;
        var margin = GB;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertDoesNotThrow(() -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMarginForFile(Path.of("fake"), margin, fileSize));
    }

    @Test
    void assertDirDoesNotHaveEnoughDiskSpaceMarginForFile() throws IOException, NotEnoughDiskSpaceException {
        var fileService = Mockito.mock(FileService.class);
        // file is just over 1 GB, the margin is 1 GB and the amount of disk space is 2 GB
        var fileSize = MB*1026;
        var margin = GB;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertThrows(NotEnoughDiskSpaceException.class, () -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMarginForFile(Path.of("fake"), margin, fileSize));
    }

    @Test
    void assertDirDoesNotHaveEnoughDiskSpaceAtAll() throws IOException, NotEnoughDiskSpaceException {
        var fileService = Mockito.mock(FileService.class);
        // file is 3 GB, the margin is 1 GB and the amount of disk space is 2 GB, but the margin should not even matter now
        var fileSize = GB*3;
        var margin = 0;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertThrows(NotEnoughDiskSpaceException.class, () -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMarginForFile(Path.of("fake"), margin, fileSize));
    }


    @Test
    void assertDirJustHasEnoughDiskSpace() throws IOException, NotEnoughDiskSpaceException {
        var fileService = Mockito.mock(FileService.class);
        // file is almost 1 GB, the margin is 1 GB and the amount of disk space is 2 GB, but the margin should not even matter now
        var fileSize = MB*1023;
        var margin = GB*1;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertDoesNotThrow(() -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMarginForFile(Path.of("fake"), margin, fileSize));
    }

    @Test
    void assertDirShouldNotCheck() throws IOException, NotEnoughDiskSpaceException {
        var fileService = Mockito.mock(FileService.class);
        // if content length is -1, no checks are done, even though the margin exceeds the available disk space
        var fileSize = -1;
        var margin = GB*3;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertDoesNotThrow(() -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMarginForFile(Path.of("fake"), margin, fileSize));
    }

    @Test
    void assertDirHasEnoughDiskspaceMargin() throws IOException {
        var fileService = Mockito.mock(FileService.class);
        var margin = GB*1;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*2);
        assertDoesNotThrow(() -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMargin(Path.of("fake"), margin));
    }

    @Test
    void assertDirDoesNotHaveEnoughDiskspaceMargin() throws IOException {
        var fileService = Mockito.mock(FileService.class);
        var margin = GB*1;
        Mockito.when(fileService.getAvailableDiskSpace(Mockito.any())).thenReturn(GB*1-1);
        assertThrows(NotEnoughDiskSpaceException.class, () -> new FilesystemSpaceVerifierImpl(fileService).assertDirHasEnoughDiskspaceMargin(Path.of("fake"), margin));
    }
}
