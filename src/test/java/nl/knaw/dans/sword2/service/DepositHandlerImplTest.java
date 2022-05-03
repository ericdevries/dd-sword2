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

import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.DepositState;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.config.Sword2Config;
import nl.knaw.dans.sword2.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.exceptions.InvalidPartialFileException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class DepositHandlerImplTest {
    final FileService fileService = new FileServiceImpl();
    final ChecksumCalculator checksumCalculator = new ChecksumCalculatorImpl();
    final ZipService zipService = new ZipServiceImpl(fileService);
    final BagItManager bagItManager = new BagItManagerImpl(fileService, checksumCalculator);
    final BagExtractor bagExtractor = new BagExtractorImpl(zipService, fileService, bagItManager);
    final DepositPropertiesManager depositPropertiesManager = new DepositPropertiesManagerImpl();
    final CollectionManager collectionManager = Mockito.mock(CollectionManager.class);
    final UserManager userManager = Mockito.mock(UserManager.class);
    final BlockingQueue queue = Mockito.mock(BlockingQueue.class);

    @BeforeEach
    void beforeEach() throws IOException {
        FileUtils.deleteDirectory(new File("data/tmp/deposithandler/"));
    }

    Path createDepositFrom(String name, String id, DepositState state) throws IOException {
        var bagName = "/zips/" + name;
        var p = getClass().getResource(bagName);
        assert p != null;
        var path = Path.of(p.getPath());

        fileService.ensureDirectoriesExist(Path.of("data/tmp/deposithandler/uploads", id));
        fileService.ensureDirectoriesExist(Path.of("data/tmp/deposithandler/deposits"));

        var deposit = new Deposit();
        deposit.setId(id);
        deposit.setCollectionId("deposithandler");
        deposit.setInProgress(true);
        deposit.setFilename(name);
        deposit.setState(DepositState.DRAFT);
        deposit.setStateDescription("Deposit is open for additional data");
        deposit.setCreated(OffsetDateTime.now());
        deposit.setMimeType("application/zip");

        // now store these properties
        // set state to draft
        depositPropertiesManager.saveProperties(Path.of("data/tmp/deposithandler/uploads/testid"), deposit);

        fileService.copyFile(path, Path.of("data/tmp/deposithandler/uploads", id, name));

        return path;
    }

    @Test
    void finalizeDeposit() throws InvalidDepositException, InvalidPartialFileException, DepositNotFoundException, Exception, CollectionNotFoundException {
        var config = new Sword2Config();
        var collectionConfig = new CollectionConfig();
        collectionConfig.setName("collection1");
        collectionConfig.setPath("6");
        collectionConfig.setUploads(Path.of("data/tmp/deposithandler/uploads"));
        collectionConfig.setDeposits(Path.of("data/tmp/deposithandler/deposits"));
        config.setCollections(List.of(collectionConfig));

        var depositor = new Depositor();
        depositor.setName("user001");
        depositor.setFilepathMapping(true);

        Mockito.when(collectionManager.getCollections()).thenReturn(config.getCollections());
        Mockito.when(collectionManager.getCollectionByName(Mockito.any())).thenReturn(collectionConfig);

        Mockito.when(userManager.getDepositorById(Mockito.any())).thenReturn(depositor);

        createDepositFrom("audiences.zip", "testid", DepositState.DRAFT);

        var depositHandler = new DepositHandlerImpl(config,
            bagExtractor,
            fileService,
            depositPropertiesManager, collectionManager, userManager, queue, bagItManager);

        depositHandler.finalizeDeposit("testid");

        assertEquals(1, 2);
    }
}
