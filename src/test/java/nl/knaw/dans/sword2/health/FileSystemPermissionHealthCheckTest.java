package nl.knaw.dans.sword2.health;

import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.core.DepositState;
import nl.knaw.dans.sword2.core.service.FileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemPermissionHealthCheckTest {

    @Test
    void checkEverythingWorks() throws Exception {
        var collection1 = new CollectionConfig("name", "path", Path.of("uploads"), Path.of("deposits"), 100, List.of(DepositState.INVALID));
        var collection2 = new CollectionConfig("name2", "path2", Path.of("uploads2"), Path.of("deposits2"), 100, List.of(DepositState.INVALID));
        var collections = List.of(collection1, collection2);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(fileService.canWriteTo(Mockito.any())).thenReturn(true);

        var result = new FileSystemPermissionHealthCheck(collections, fileService).check();

        assertTrue(result.isHealthy());
    }

    @Test
    void checkEverythingWorksExceptForOnePath() throws Exception {
        var collection1 = new CollectionConfig("name", "path", Path.of("uploads"), Path.of("deposits"), 100, List.of(DepositState.INVALID));
        var collection2 = new CollectionConfig("name2", "path2", Path.of("uploads2"), Path.of("deposits2"), 100, List.of(DepositState.INVALID));
        var collections = List.of(collection1, collection2);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(fileService.canWriteTo(Mockito.any())).thenReturn(true);
        Mockito.when(fileService.canWriteTo(Mockito.eq(Path.of("deposits")))).thenReturn(false);

        var result = new FileSystemPermissionHealthCheck(collections, fileService).check();

        assertFalse(result.isHealthy());

        Mockito.verify(fileService).canWriteTo(Path.of("uploads"));
        Mockito.verify(fileService).canWriteTo(Path.of("deposits"));
        Mockito.verify(fileService).canWriteTo(Path.of("uploads2"));
        Mockito.verify(fileService).canWriteTo(Path.of("deposits2"));
        Mockito.verifyNoMoreInteractions(fileService);
    }

    @Test
    void checkEverythingWorksExceptForOnePathUploads() throws Exception {
        var collection1 = new CollectionConfig("name", "path", Path.of("uploads"), Path.of("deposits"), 100, List.of(DepositState.INVALID));
        var collection2 = new CollectionConfig("name2", "path2", Path.of("uploads2"), Path.of("deposits2"), 100, List.of(DepositState.INVALID));
        var collections = List.of(collection1, collection2);
        var fileService = Mockito.mock(FileService.class);

        Mockito.when(fileService.canWriteTo(Mockito.any())).thenReturn(true);
        Mockito.when(fileService.canWriteTo(Mockito.eq(Path.of("uploads")))).thenReturn(false);

        var result = new FileSystemPermissionHealthCheck(collections, fileService).check();

        assertFalse(result.isHealthy());

        Mockito.verify(fileService).canWriteTo(Path.of("uploads"));
        Mockito.verify(fileService).canWriteTo(Path.of("deposits"));
        Mockito.verify(fileService).canWriteTo(Path.of("uploads2"));
        Mockito.verify(fileService).canWriteTo(Path.of("deposits2"));
        Mockito.verifyNoMoreInteractions(fileService);
    }
}