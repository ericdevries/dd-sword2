package nl.knaw.dans.sword2.resource;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.sword2.DdSword2Application;
import nl.knaw.dans.sword2.DdSword2Configuration;
import nl.knaw.dans.sword2.service.FileServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@ExtendWith(DropwizardExtensionsSupport.class)
class CollectionHandlerImplTest {
    //
    //    private static final FileService fileService = new FileServiceImpl();
    //    private static final ZipService zipService = new ZipServiceImpl(fileService);
    //    private static final BagExtractor bagExtractor = new BagExtractorImpl(zipService);
    //    private static final DepositPropertiesManager depositPropertiesManager = new DepositPropertiesManagerImpl();
    private static DropwizardAppExtension<DdSword2Configuration> EXT = new DropwizardAppExtension<>(
        DdSword2Application.class,
        ResourceHelpers.resourceFilePath("debug-etc/config.yml")
    );
    //    private static final DepositManager depositManager = new DepositManagerImpl(EXT.getConfiguration().getSword2(), bagExtractor, fileService, depositPropertiesManager);

    //    private static final ResourceExtension EXT = ResourceExtension.builder().addResource(new CollectionHandlerImpl(depositManager)).build();

    @BeforeEach
    void startUp() throws IOException {
        new FileServiceImpl().ensureDirectoriesExist(Path.of("data/tmp/1"));
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(Path.of("data/tmp").toFile());
    }

    @Test
    void testZipDepositWithoutSlug() throws IOException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = EXT.client().target(url)
            .request()
            .header("content-type", "application/zip")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        var paths = Files.walk(Path.of("data/tmp/1/deposits/")).collect(Collectors.toList());
        var firstPath = paths.get(1); // the path at 0 is the directory itself, we need the first child

        System.out.println("PATH: " + firstPath);
        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));
    }

    @Test
    void testZipDepositWithSlug() throws IOException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = EXT.client().target(url)
            .request()
            .header("content-type", "application/zip")
            .header("slug", "the_slug")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        var targetPath = Path.of("data/tmp/1/deposits/the_slug");
        assertTrue(Files.exists(targetPath), "Expected path not found");
        assertTrue(Files.exists(targetPath.resolve("deposit.properties")));
    }
}
