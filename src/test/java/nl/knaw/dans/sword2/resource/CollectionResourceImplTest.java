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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
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
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class CollectionResourceImplTest {

    private static DropwizardAppExtension<DdSword2Configuration> EXT = new DropwizardAppExtension<>(
        DdSword2Application.class,
        ResourceHelpers.resourceFilePath("debug-etc/config.yml")
    );

    @BeforeEach
    void startUp() throws IOException {
        new FileServiceImpl().ensureDirectoriesExist(Path.of("data/tmp/1"));
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(Path.of("data/tmp")
            .toFile());
    }

    Builder buildRequest(String path) {
        var url = String.format("http://localhost:%s%s", EXT.getLocalPort(), path);

        return EXT.client()
            .target(url)
            .register(MultiPartFeature.class)
            .request()
            .header("authorization", "Basic dXNlcjAwMTp1c2VyMDAx");
    }

    @Test
    void testZipDepositDraftState() throws IOException, JAXBException, ConfigurationException {
        var path = getClass().getResource("/zips/audiences.zip");

        assert path != null;

        var result = buildRequest("/collection/1")
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("content-disposition", "attachment; filename=bag.zip")
            .header("in-progress", "true")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var receipt = result.readEntity(Entry.class);
        var parts = receipt.getId()
            .split("/");
        var id = parts[parts.length - 1];

        var firstPath = Path.of("data/tmp/1/uploads/", id);

        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));
        assertTrue(Files.exists(firstPath.resolve("bag.zip")));

        var config = getProperties(firstPath);

        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("DRAFT", config.getString("state.label"));
        assertEquals("user001", config.getString("depositor.userId"));
        assertEquals("Deposit is open for additional data", config.getString("state.description"));
        //assertEquals("AUDIENCE", config.getString("bag-store.bag-name"));

        var statusResult = buildRequest("/statement/" + id)
            .get();

        var feed = statusResult.readEntity(Feed.class);

    }

    @Test
    void testZipInParts() throws IOException, JAXBException, ConfigurationException {
        var path = getClass().getResource("/zips/audiences.zip");
        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        assert path != null;

        var result = buildRequest("/collection/1")
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("content-disposition", "attachment; filename=bag.zip")
            .header("in-progress", "true")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var receipt = result.readEntity(Entry.class);
        var parts = receipt.getId()
            .split("/");
        var id = parts[parts.length - 1];
        var firstPath = Path.of("data/tmp/1/uploads/", id);

        assertTrue(Files.exists(firstPath.resolve("deposit.properties")));
        assertTrue(Files.exists(firstPath.resolve("bag.zip")));

        var config = getProperties(firstPath);

        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("DRAFT", config.getString("state.label"));
        //        assertNotNull(config.getString("deposit.userId"));
        assertEquals("Deposit is open for additional data", config.getString("state.description"));
        //        assertEquals("AUDIENCE", config.getString("bag-store.bag-name"));

        var statementUrl = String.format("http://localhost:%s/statement/%s", EXT.getLocalPort(),
            parts[parts.length - 1]);

        var statusResult = buildRequest("/statement/" + parts[parts.length - 1])
            .get();

        var feed = statusResult.readEntity(Feed.class);

    }

    @Test
    void testZipDepositUploaded()
        throws IOException, JAXBException, ConfigurationException, InterruptedException {
        var path = getClass().getResource("/zips/audiences.zip");

        assert path != null;

        var result = buildRequest("/collection/1")
            .header("content-type", "application/zip")
            .header("content-md5", "bc27e20467a773501a4ae37fb85a9c3f")
            .header("in-progress", "false")
            .header("content-disposition", "attachment; filename=bag.zip")
            .post(Entity.entity(path.openStream(), MediaType.valueOf("application/zip")));

        assertEquals(201, result.getStatus());

        var receipt = result.readEntity(Entry.class);
        var parts = receipt.getId()
            .split("/");
        var id = parts[parts.length - 1];

        var count = 0;
        var state = "";

        // waiting at most 5 seconds for the background thread to handle this
        while (count < 5) {

            var statement = buildRequest("/statement/" + id)
                .get(Feed.class);

            state = statement.getCategory()
                .getTerm();

            if (state.equals("SUBMITTED")) {
                break;
            }
            Thread.sleep(1000);
            count += 1;
        }

        assertEquals("SUBMITTED", state);

        var firstPath = Path.of("data/tmp/1/deposits/" + id);
        var config = getProperties(firstPath);

        assertNotNull(config.getString("bag-store.bag-id"));
        assertNotNull(config.getString("dataverse.bag-id"));
        assertNotNull(config.getString("creation.timestamp"));
        assertEquals("SWORD2", config.getString("deposit.origin"));
        assertEquals("SUBMITTED", config.getString("state.label"));
        assertEquals("user001", config.getString("depositor.userId"));
        assertEquals("Deposit is valid and ready for post-submission processing", config.getString("state.description"));
        assertEquals("audiences", config.getString("bag-store.bag-name"));

    }

    @Test
    void testMultipartZipFile() throws IOException {
        var path = getClass().getResource("/zips/audiences.zip");
        assert path != null;

        var multiPart = new MultiPart();
        var payloadPart = new BodyPart(MediaType.valueOf("application/zip"));
        payloadPart.getHeaders()
            .add("content-disposition", "attachment; filename=bag.zip; name=payload");
        payloadPart.getHeaders()
            .add("content-md5", "bc27e20467a773501a4ae37fb85a9c3f");
        payloadPart.getHeaders()
            .add("packaging", "http://purl.org/net/sword/package/BagIt");
        payloadPart.entity(path.openStream());

        multiPart.bodyPart(payloadPart);
        multiPart.setMediaType(MediaType.valueOf("multipart/related"));

        var url = String.format("http://localhost:%s/collection/1", EXT.getLocalPort());

        var result = buildRequest("/collection/1")
            .header("content-length", 1000)
            .header("in-progress", "true")
            .post(Entity.entity(multiPart, multiPart.getMediaType()));

        assertEquals(201, result.getStatus());
        var receipt = result.readEntity(Entry.class);
        var parts = receipt.getId()
            .split("/");
        var id = parts[parts.length - 1];
        var firstPath = Path.of("data/tmp/1/uploads/", id);
    }

    FileBasedConfiguration getProperties(Path path) throws ConfigurationException {
        var params = new Parameters();
        var paramConfig = params.properties()
            .setFileName(path.resolve("deposit.properties")
                .toString());

        var builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(
            PropertiesConfiguration.class, null, true).configure(
            paramConfig);

        return builder.getConfiguration();
    }
}
