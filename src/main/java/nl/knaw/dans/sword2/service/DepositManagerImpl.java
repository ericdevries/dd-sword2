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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Singleton
public class DepositManagerImpl implements DepositManager {
    private final String tempDir = "data/temp";
    private final String rootDir = "data/deposits";
    private final String archivedRootDir = "data/deposits-archived";
    private final BagExtractor bagExtractor;
    private final FileService fileService;
    private final DepositPropertiesManager depositPropertiesManager;

    @Inject
    public DepositManagerImpl(BagExtractor bagExtractor, FileService fileService, DepositPropertiesManager depositPropertiesManager) {
        this.bagExtractor = bagExtractor;
        this.fileService = fileService;
        this.depositPropertiesManager = depositPropertiesManager;
    }

    @Override
    public Path storeDepositContent( Deposit deposit, InputStream inputStream) throws IOException {
        var tempPath = Path.of(tempDir).resolve("SWORD-" + deposit.getId());
        fileService.copyFile(inputStream, tempPath);
        return tempPath;
    }

    @Override
    public void createDeposit(Deposit deposit, Path payload) throws IOException {
        var id = deposit.getCanonicalId();
        var depositPath = getDepositPath(id);
        fileService.ensureDirectoriesExist(depositPath);

        var props = depositPropertiesManager.getProperties(deposit);

        props.setBagStoreBagId(id);
        props.setDataverseBagId(String.format("urn:uuid:%s", id));
        props.setCreationTimestamp(OffsetDateTime.now()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        props.setDepositOrigin("SWORD2");
        props.setState(DepositState.SUBMITTED);

        System.out.println("PROPS: " + props);

        depositPropertiesManager.saveProperties(deposit, props);

        // now unzip bag

        bagExtractor.extractBag(payload, depositPath, true);
    }

    private Path getDepositPath(String id) {
        return Path.of(rootDir, id);
    }

    @Override
    public void setDepositState(Deposit deposit, DepositState state) {

    }

    String getDepositId(Deposit deposit) {
        if (deposit.getSlug() != null) {
            return deposit.getSlug();
        }

        return deposit.getId();
    }

    Path getPayloadPath(String id, Path path) {
        var depositPath = getDepositPath(id);
        return depositPath.resolve(path.getFileName());
    }

}
