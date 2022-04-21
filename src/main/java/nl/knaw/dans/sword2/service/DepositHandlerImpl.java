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
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.config.Sword2Config;
import nl.knaw.dans.sword2.exceptions.HashMismatchException;
import nl.knaw.dans.sword2.exceptions.InvalidContentDispositionException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class DepositHandlerImpl implements DepositHandler {

    private static final Logger log = LoggerFactory.getLogger(DepositHandlerImpl.class);

    private final BagExtractor bagExtractor;
    private final FileService fileService;
    private final DepositPropertiesManager depositPropertiesManager;
    private final Sword2Config sword2Config;
    private final ChecksumCalculator checksumCalculator;

    public DepositHandlerImpl(Sword2Config sword2Config,
        BagExtractor bagExtractor,
        FileService fileService,
        DepositPropertiesManager depositPropertiesManager,
        ChecksumCalculator checksumCalculator) {
        this.sword2Config = sword2Config;
        this.bagExtractor = bagExtractor;
        this.fileService = fileService;
        this.depositPropertiesManager = depositPropertiesManager;
        this.checksumCalculator = checksumCalculator;
    }

    @Override
    public Path storeDepositContent(Deposit deposit, InputStream inputStream)
        throws IOException, NotEnoughDiskSpaceException, NoSuchAlgorithmException, HashMismatchException, InvalidContentDispositionException {
        assertFilenameIsNotEmpty(deposit);

        var collection = getCollection(deposit);
        var tempPath = collection.getUploads()
            .resolve(deposit.getId())
            .resolve(deposit.getFilename());

        fileService.ensureDirectoriesExist(collection.getUploads());

        assertTempDirHasEnoughDiskspaceMarginForFile(collection.getUploads(), deposit.getContentLength());

        log.debug("Storing deposit payload in {}", tempPath);
        fileService.copyFile(inputStream, tempPath);

        assertHashMatches(tempPath, deposit.getMd5());

        return tempPath;
    }

    void assertFilenameIsNotEmpty(Deposit deposit) throws InvalidContentDispositionException {
        if (deposit.getFilename() == null || "".equals(deposit.getFilename().strip())) {
            throw new InvalidContentDispositionException("No file name provided");
        }
    }

    void assertHashMatches(Path path, String hash) throws HashMismatchException, IOException, NoSuchAlgorithmException {
        var checksum = checksumCalculator.calculateMD5Checksum(path);

        if (!checksum.equals(hash)) {
            throw new HashMismatchException(String.format("Hash %s does not match expected hash %s", checksum, hash));
        }
    }

    @Override
    public DepositProperties createDeposit(Deposit deposit, Path payload) throws IOException {
        var collection = getCollection(deposit);
        var id = deposit.getCanonicalId();
        var path = getUploadPath(collection, id);

        fileService.ensureDirectoriesExist(path);

        var props = depositPropertiesManager.getProperties(path, deposit);

        props.setBagStoreBagId(id);
        props.setDataverseBagId(String.format("urn:uuid:%s", id));
        props.setContentType(deposit.getMimeType());
        props.setCreationTimestamp(OffsetDateTime.now()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        props.setDepositOrigin("SWORD2");

        setDepositState(props, DepositState.DRAFT, null);

        depositPropertiesManager.saveProperties(path, deposit, props);

        return props;
    }

    void finalizeDeposit() {
        // if deposit is not in progress
        // set state to UPLOADED

        // asynchronous:
        // set state to FINALIZING
        // extract bag (including checking for disk size)
        // set state to SUBMITTED
        // set bag name based on filename of content-disposition
        // get sword token
        // get otherId and otherVersion
        // set these properties
        // remove zip files
        // delete content-type property
        // move bag to storage (deposits folder)

    }


    void assertTempDirHasEnoughDiskspaceMarginForFile(Path destination, long contentLength) throws IOException, NotEnoughDiskSpaceException {
        if (contentLength > -1) {
            var availableSpace = fileService.getAvailableDiskSpace(destination);
            log.debug("Free space  = {}", availableSpace);
            log.debug("File length = {}", contentLength);
            log.debug("Margin      = {}", sword2Config.getDiskSpaceMargin());
            log.debug("Extra space = {}", availableSpace - contentLength - sword2Config.getDiskSpaceMargin());

            if (availableSpace - contentLength < sword2Config.getDiskSpaceMargin()) {
                throw new NotEnoughDiskSpaceException("Not enough space available");
            }
        }
        else {
            log.debug("Content-length is -1, not checking for disk space margin");
        }
    }

    private Path getDepositPath(CollectionConfig collectionConfig, String id) {
        return collectionConfig.getDeposits()
            .resolve(id);
    }

    private Path getUploadPath(CollectionConfig collectionConfig, String id) {
        return collectionConfig.getUploads()
            .resolve(id);
    }

//    @Override
    public void setDepositState(Path path, Deposit deposit, DepositState state) {
        var properties = depositPropertiesManager.getProperties(path, deposit);
        setDepositState(properties, state, null);
        depositPropertiesManager.saveProperties(path, deposit, properties);
    }

//    @Override
    public DepositProperties getDepositProperties(Path path, Deposit deposit) {
        return depositPropertiesManager.getProperties(path, deposit);
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
