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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.DepositState;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.config.Sword2Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositHandlerImpl implements DepositHandler {

    private static final Logger log = LoggerFactory.getLogger(DepositHandlerImpl.class);

    private final BagExtractor bagExtractor;
    private final FileService fileService;
    private final DepositPropertiesManager depositPropertiesManager;
    private final Sword2Config sword2Config;

    public DepositHandlerImpl(Sword2Config sword2Config,
        BagExtractor bagExtractor,
        FileService fileService,
        DepositPropertiesManager depositPropertiesManager
    ) {
        this.sword2Config = sword2Config;
        this.bagExtractor = bagExtractor;
        this.fileService = fileService;
        this.depositPropertiesManager = depositPropertiesManager;
    }

    @Override
    public Path storeDepositContent(Deposit deposit, InputStream inputStream) throws IOException {
        var collection = getCollection(deposit);
        var tempPath = collection.getUploads()
            .resolve("SWORD-" + deposit.getId());
        log.debug("Storing deposit payload in {}", tempPath);
        fileService.copyFile(inputStream, tempPath);
        return tempPath;
    }

    @Override
    public DepositProperties createDeposit(Deposit deposit, Path payload) throws IOException {
        var collection = getCollection(deposit);
        var id = deposit.getCanonicalId();
        var depositPath = getDepositPath(collection, id);
        fileService.ensureDirectoriesExist(depositPath);

        var props = depositPropertiesManager.getProperties(deposit);

        props.setBagStoreBagId(id);
        props.setDataverseBagId(String.format("urn:uuid:%s", id));
        props.setCreationTimestamp(OffsetDateTime.now()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        props.setDepositOrigin("SWORD2");
        setDepositState(props, DepositState.DRAFT, null);

        depositPropertiesManager.saveProperties(deposit, props);

        // now unzip bag

        bagExtractor.extractBag(payload, depositPath, true);

        return props;
    }

    private Path getDepositPath(CollectionConfig collectionConfig, String id) {
        return collectionConfig.getDeposits()
            .resolve(id);
    }

    @Override
    public void setDepositState(Deposit deposit, DepositState state) {
        var properties = depositPropertiesManager.getProperties(deposit);
        setDepositState(properties, state, null);
        depositPropertiesManager.saveProperties(deposit, properties);
    }

    @Override
    public DepositProperties getDepositProperties(Deposit deposit) {
        return depositPropertiesManager.getProperties(deposit);
    }

    // TODO make this work
    private CollectionConfig getCollection(Deposit deposit) {
        return this.sword2Config.getCollections()
            .get(0);
    }

    void setDepositState(DepositProperties properties, DepositState state, String message) {
        properties.setState(state);

        switch (state) {
            case DRAFT:
                properties.setStateDescription("Deposit is open for additional data");
                break;
            case SUBMITTED:
                properties.setStateDescription("Deposit is valid and ready for post-submission processing");
                break;
            case FAILED:
                // TODO generic error message
                properties.setStateDescription("TODO error message");
                break;
            case INVALID:
                properties.setStateDescription(message == null ? "Unknown" : message);
                break;
            case ARCHIVED:
                properties.setStateDescription("Deposit is archived");
                break;
            case REJECTED:
                // TODO check
                properties.setStateDescription("Unknown");
                break;
            case UPLOADED:
                // TODO if it is rescheduled due to failed diskspace requirements, the message should be different
                properties.setStateDescription("Deposit upload has been completed");
                break;
            case FINALIZING:
                properties.setStateDescription("Finalizing deposit");
                break;
        }
    }
}
