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
package nl.knaw.dans.sword2.resource;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.sword2.DdSword2Application;
import nl.knaw.dans.sword2.DdSword2Configuration;
import nl.knaw.dans.sword2.models.entry.Entry;
import nl.knaw.dans.sword2.models.statement.Feed;
import nl.knaw.dans.sword2.service.FileServiceImpl;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        //        FileUtils.deleteDirectory(Path.of("data/tmp")
        //            .toFile());
    }

    @Test
    void testZipDepositDraftState() throws IOException, JAXBException, ConfigurationException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = EXT.client()
            .target(url)
            .request()
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("content-disposition", "attachment; filename=bag.zip")
            .header("in-progress", "true")
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var paths = Files.walk(Path.of("data/tmp/1/uploads/"))
            .collect(Collectors.toList());
        var firstPath = paths.get(1); // the path at 0 is the directory itself, we need the first child

        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));
        assertTrue(Files.exists(firstPath.resolve("bag.zip")));

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(firstPath.resolve("deposit.properties").toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        var config = builder.getConfiguration();

        System.out.println("CONFIG: " + config);
        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("DRAFT", config.getString("state.label"));
        //        assertNotNull(config.getString("deposit.userId"));
        assertEquals("Deposit is open for additional data", config.getString("state.description"));
        //        assertEquals("AUDIENCE", config.getString("bag-store.bag-name"));

        var receipt = result.readEntity(Entry.class);
        System.out.println("RECEIPT " + receipt);

        var parts = receipt.getId().split("/");

        var statementUrl =  String.format("http://localhost:%s/statement/%s", EXT.getLocalPort(),
            parts[parts.length - 1]);

        var statusResult = EXT.client()
            .target(statementUrl)
            .request()
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .get();

        var feed = statusResult.readEntity(Feed.class);

        System.out.println("STATUS: " + feed);

    }

    @Test
    void testZipInParts() throws IOException, JAXBException, ConfigurationException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = EXT.client()
            .target(url)
            .request()
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("content-disposition", "attachment; filename=bag.zip")
            .header("in-progress", "true")
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var paths = Files.walk(Path.of("data/tmp/1/uploads/"))
            .collect(Collectors.toList());
        var firstPath = paths.get(1); // the path at 0 is the directory itself, we need the first child

        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));
        assertTrue(Files.exists(firstPath.resolve("bag.zip")));

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(firstPath.resolve("deposit.properties").toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        var config = builder.getConfiguration();

        System.out.println("CONFIG: " + config);
        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("DRAFT", config.getString("state.label"));
        //        assertNotNull(config.getString("deposit.userId"));
        assertEquals("Deposit is open for additional data", config.getString("state.description"));
        //        assertEquals("AUDIENCE", config.getString("bag-store.bag-name"));

        var receipt = result.readEntity(Entry.class);
        System.out.println("RECEIPT " + receipt);

        var parts = receipt.getId().split("/");

        var statementUrl =  String.format("http://localhost:%s/statement/%s", EXT.getLocalPort(),
            parts[parts.length - 1]);

        var statusResult = EXT.client()
            .target(statementUrl)
            .request()
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .get();

        var feed = statusResult.readEntity(Feed.class);

        System.out.println("STATUS: " + feed);

    }

    @Test
    void testZipDepositUploaded() throws IOException, JAXBException, ConfigurationException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = EXT.client()
            .target(url)
            .request()
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("in-progress", "false")
            .header("content-disposition", "attachment; filename=bag.zip")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var paths = Files.walk(Path.of("data/tmp/1/uploads/"))
            .collect(Collectors.toList());
        var firstPath = paths.get(1); // the path at 0 is the directory itself, we need the first child

        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));

        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(firstPath.resolve("deposit.properties").toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        var config = builder.getConfiguration();

        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("UPLOADED", config.getString("state.label"));
        //        assertNotNull(config.getString("deposit.userId"));
        assertEquals("Deposit upload has been completed", config.getString("state.description"));
        //        assertEquals("AUDIENCE", config.getString("bag-store.bag-name"));

    }

    @Test
    void testMultipartZipFile() throws IOException {
        var path = getClass().getResource("/zips/audiences.zip");
        assert path != null;

        var multiPart = new MultiPart();
        var payloadPart = new BodyPart(MediaType.valueOf("application/zip"));
        payloadPart.getHeaders().add("content-disposition", "attachment; filename=bag.zip; name=payload");
        payloadPart.getHeaders().add("content-md5", "bc27e20467a773501a4ae37fb85a9c3f");
        payloadPart.getHeaders().add("packaging", "http://purl.org/net/sword/package/BagIt");
        payloadPart.entity(path.openStream());

        multiPart.bodyPart(payloadPart);
        multiPart.setMediaType(MediaType.valueOf("multipart/related"));

        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        //        url = "http://localhost:20100/collection/1";

        var result = EXT.client()
            .register(MultiPartFeature.class)
            .target(url)
            .request()
            .header("content-length", 1000)
            .header("in-progress", "true")
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            //            .header("authorization", "Basic YTp1c2VyMDAxxx")
            .post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertEquals(201, result.getStatus());
        System.out.println("RESULT: " + result);
    }
}
