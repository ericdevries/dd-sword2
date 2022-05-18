package nl.knaw.dans.sword2.health;

import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.core.DepositState;
import nl.knaw.dans.sword2.core.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UploadDepositOnSameFileSystemHealthCheckTest {

    @Test
    void check() throws Exception {
        var collection1 = new CollectionConfig("name", "path", Path.of("uploads"), Path.of("deposits"), 100, List.of(DepositState.INVALID));
        var collection2 = new CollectionConfig("name2", "path2", Path.of("uploads2"), Path.of("deposits2"), 100, List.of(DepositState.INVALID));

        var collections = List.of(collection1, collection2);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(fileService.isSameFileSystem(Path.of("uploads"), Path.of("deposits"))).thenReturn(true);
        Mockito.when(fileService.isSameFileSystem(Path.of("uploads2"), Path.of("deposits2"))).thenReturn(true);

        var result = new UploadDepositOnSameFileSystemHealthCheck(collections, fileService).check();

        assertTrue(result.isHealthy());
    }

    @Test
    void checkOneIsInvalid() throws Exception {
        var collection1 = new CollectionConfig("name", "path", Path.of("uploads"), Path.of("deposits"), 100, List.of(DepositState.INVALID));
        var collection2 = new CollectionConfig("name2", "path2", Path.of("uploads2"), Path.of("deposits2"), 100, List.of(DepositState.INVALID));

        var collections = List.of(collection1, collection2);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(fileService.isSameFileSystem(Path.of("uploads"), Path.of("deposits"))).thenReturn(true);
        Mockito.when(fileService.isSameFileSystem(Path.of("uploads2"), Path.of("deposits2"))).thenReturn(false);

        var result = new UploadDepositOnSameFileSystemHealthCheck(collections, fileService).check();

        assertFalse(result.isHealthy());
    }
}