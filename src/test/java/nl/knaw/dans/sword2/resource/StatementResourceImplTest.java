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
import nl.knaw.dans.sword2.api.statement.Feed;
import nl.knaw.dans.sword2.core.Deposit;
import nl.knaw.dans.sword2.core.DepositState;
import nl.knaw.dans.sword2.core.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.core.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.core.service.DepositPropertiesManagerImpl;
import nl.knaw.dans.sword2.core.service.FileServiceImpl;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
class StatementResourceImplTest {
    private DropwizardAppExtension<DdSword2Configuration> EXT = new DropwizardAppExtension<>(
        DdSword2Application.class,
        ResourceHelpers.resourceFilePath("test-etc/config-regular.yml")
    );

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.deleteDirectory(Path.of("data/tmp").toFile());
        new FileServiceImpl().ensureDirectoriesExist(Path.of("data/tmp/1"));
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(Path.of("data/tmp").toFile());
    }

    @Test
    void testStatement() throws DepositNotFoundException, InvalidDepositException {
        var deposit = new Deposit();
        deposit.setId("a03ca6f1-608b-4247-8c22-99681b8494a0");
        deposit.setCreated(OffsetDateTime.of(2022, 5, 1, 1, 2, 3, 4, ZoneOffset.UTC));
        deposit.setState(DepositState.SUBMITTED);
        deposit.setStateDescription("Submitted");
        deposit.setDepositor("user001");

        new DepositPropertiesManagerImpl().saveProperties(Path.of("data/tmp/1/deposits/a03ca6f1-608b-4247-8c22-99681b8494a0"), deposit);

        var url = String.format("http://localhost:%s/statement/a03ca6f1-608b-4247-8c22-99681b8494a0", EXT.getLocalPort());
        var response = EXT.client()
            .target(url)
            .request()
            .header("Authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .get();

        assertEquals(200, response.getStatus());

        var feed = response.readEntity(Feed.class);

        assertEquals("http://localhost:20320/statement/a03ca6f1-608b-4247-8c22-99681b8494a0", feed.getId());
        assertEquals("SUBMITTED", feed.getCategory().getTerm());

        var hash = response.getHeaderString("content-md5");
        assertEquals("30d203e2d0c5e349921a8317f66b759b", hash);
    }

    @Test
    void testStatementForUnknownDeposit() throws DepositNotFoundException, InvalidDepositException {
        var url = String.format("http://localhost:%s/statement/a03ca6f1-608b-4247-8c22-99681b8494a0", EXT.getLocalPort());
        var response = EXT.client()
            .target(url)
            .request()
            .header("Authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .get();

        assertEquals(404, response.getStatus());
    }

    @Test
    void testStatementForInvalidDeposit() throws DepositNotFoundException, InvalidDepositException {
        var deposit = new Deposit();
        deposit.setId("a03ca6f1-608b-4247-8c22-99681b8494a0");
        deposit.setCreated(OffsetDateTime.of(2022, 5, 1, 1, 2, 3, 4, ZoneOffset.UTC));
        deposit.setState(DepositState.SUBMITTED);
        deposit.setStateDescription("Submitted");
        deposit.setDepositor("user001");

        new DepositPropertiesManagerImpl().saveProperties(Path.of("data/tmp/1/deposits/a03ca6f1-608b-4247-8c22-99681b8494a0/subfolder"), deposit);

        var url = String.format("http://localhost:%s/statement/a03ca6f1-608b-4247-8c22-99681b8494a0", EXT.getLocalPort());
        var response = EXT.client()
            .target(url)
            .request()
            .header("Authorization", "Basic dXNlcjAwMTp1c2VyMDAx")
            .get();

        assertEquals(400, response.getStatus());
    }
}
