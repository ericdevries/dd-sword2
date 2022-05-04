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
import nl.knaw.dans.sword2.UriRegistry;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.config.Sword2Config;
import nl.knaw.dans.sword2.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositReadOnlyException;
import nl.knaw.dans.sword2.exceptions.HashMismatchException;
import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.exceptions.InvalidPartialFileException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.service.finalizer.DepositFinalizerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DepositHandlerImpl implements DepositHandler {

    private static final Logger log = LoggerFactory.getLogger(DepositHandlerImpl.class);

    private final BagExtractor bagExtractor;
    private final FileService fileService;
    private final DepositPropertiesManager depositPropertiesManager;
    private final Sword2Config sword2Config;
    private final CollectionManager collectionManager;
    private final UserManager userManager;
    private final BlockingQueue<DepositFinalizerEvent> depositFinalizerQueue;
    private final BagItManager bagItManager;

    public DepositHandlerImpl(Sword2Config sword2Config, BagExtractor bagExtractor, FileService fileService, DepositPropertiesManager depositPropertiesManager, CollectionManager collectionManager,
        UserManager userManager, BlockingQueue<DepositFinalizerEvent> depositFinalizerQueue, BagItManager bagItManager) {
        this.sword2Config = sword2Config;
        this.bagExtractor = bagExtractor;
        this.fileService = fileService;
        this.depositPropertiesManager = depositPropertiesManager;
        this.collectionManager = collectionManager;
        this.userManager = userManager;
        this.depositFinalizerQueue = depositFinalizerQueue;
        this.bagItManager = bagItManager;
    }

    @Override
    public Deposit createDepositWithPayload(String collectionId, Depositor depositor, boolean inProgress, MediaType contentType, String hash, String packaging, String filename, long filesize,
        InputStream inputStream) throws CollectionNotFoundException, IOException, NotEnoughDiskSpaceException, HashMismatchException {

        var id = UUID.randomUUID().toString();
        var collection = collectionManager.getCollectionByPath(collectionId, depositor);

        // make sure the upload directory exists
        fileService.ensureDirectoriesExist(collection.getUploads());
        assertTempDirHasEnoughDiskspaceMarginForFile(collection.getUploads(), filesize);

        var path = collection.getUploads().resolve(id).resolve(filename);

        var depositFolder = path.getParent();

        // check if the hash matches the one provided by the user
        var calculatedHash = fileService.copyFileWithMD5Hash(inputStream, path);

        if (hash == null || !hash.equals(calculatedHash)) {
            throw new HashMismatchException(String.format("Hash %s does not match expected hash %s", calculatedHash, hash));
        }

        var deposit = new Deposit();
        deposit.setId(id);
        deposit.setCollectionId(collectionId);
        deposit.setInProgress(inProgress);
        deposit.setFilename(filename);
        deposit.setMd5(calculatedHash);
        deposit.setPackaging(packaging);
        deposit.setContentLength(filesize);
        deposit.setDepositor(depositor.getName());
        deposit.setState(DepositState.DRAFT);
        deposit.setStateDescription("Deposit is open for additional data");
        deposit.setCreated(OffsetDateTime.now());
        deposit.setMimeType(contentType.toString());

        // now store these properties
        // set state to draft
        depositPropertiesManager.saveProperties(depositFolder, deposit);

        startFinalizingDeposit(deposit);

        return deposit;
    }

    @Override
    public Deposit addPayloadToDeposit(String depositId, Depositor depositor, boolean inProgress, MediaType contentType, String hash, String packaging, String filename, long filesize,
        InputStream inputStream) throws IOException, NotEnoughDiskSpaceException, HashMismatchException, DepositNotFoundException, DepositReadOnlyException, CollectionNotFoundException {

        var deposit = getDeposit(depositId, depositor);
        var path = deposit.getPath().resolve(filename);

        assertTempDirHasEnoughDiskspaceMarginForFile(path.getParent(), filesize);

        if (!DepositState.DRAFT.equals(deposit.getState())) {
            throw new DepositReadOnlyException(String.format("Deposit id %s is not in DRAFT state.", deposit.getId()));
        }

        // check if the hash matches the one provided by the user
        var calculatedHash = fileService.copyFileWithMD5Hash(inputStream, path);

        if (hash == null || !hash.equals(calculatedHash)) {
            throw new HashMismatchException(String.format("Hash %s does not match expected hash %s", calculatedHash, hash));
        }

        deposit.setInProgress(inProgress);
        depositPropertiesManager.saveProperties(path.getParent(), deposit);

        startFinalizingDeposit(deposit);
        return deposit;
    }

    @Override
    public DepositProperties createDeposit(Deposit deposit, Path payload) throws IOException {
        /*
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

        startFinalizingDeposit(deposit);

        return props;

         */
        return null;
    }

    @Override
    public Deposit getDeposit(String depositId, Depositor depositor) throws DepositNotFoundException {
        var deposit = getDeposit(depositId);

        if (depositor.getName().equals(deposit.getDepositor())) {
            return deposit;
        }

        throw new DepositNotFoundException(String.format("Deposit with id %s could not be found", depositId));
    }

    @Override
    public Deposit getDeposit(String depositId) throws DepositNotFoundException {
        var collections = collectionManager.getCollections();

        for (var collection : collections) {
            // TODO add more paths here (archived etc)
            var searchPaths = List.of(collection.getUploads().resolve(depositId), collection.getDeposits().resolve(depositId));

            for (var path : searchPaths) {
                System.out.println("PATH: " + path);
                if (fileService.exists(path)) {
                    var deposit = depositPropertiesManager.getProperties(path);
                    deposit.setPath(path);
                    deposit.setCollectionId(collection.getName());

                    return deposit;
                }
            }
        }

        throw new DepositNotFoundException(String.format("Deposit with id %s could not be found", depositId));
    }

    void startFinalizingDeposit(Deposit deposit) throws CollectionNotFoundException {
        // if deposit is not in progress
        // set state to UPLOADED
        if (deposit.isInProgress()) {
            log.info("Deposit is still in progress, not finalizing");
            return;
        }

        var collection = collectionManager.getCollectionByPath(deposit.getCollectionId());
        var path = getUploadPath(collection, deposit.getId());

        deposit.setState(DepositState.UPLOADED);
        depositPropertiesManager.saveProperties(path, deposit);

        try {
            depositFinalizerQueue.put(new DepositFinalizerEvent(deposit.getId()));
        }
        catch (InterruptedException e) {
            log.error("Interrupted while putting task on queue", e);
        }
    }

    @Override
    public Deposit finalizeDeposit(String depositId) throws DepositNotFoundException, Exception, InvalidDepositException, InvalidPartialFileException, CollectionNotFoundException {
        var deposit = getDeposit(depositId);
        var path = deposit.getPath();
        var depositor = userManager.getDepositorById(deposit.getDepositor());

        log.info("Finalizing deposit with id {}", depositId);
        deposit.setState(DepositState.FINALIZING);
        deposit.setStateDescription("Finalizing deposit");
        depositPropertiesManager.saveProperties(path, deposit);

        log.info("Extracting files for deposit {}", depositId);
        bagExtractor.extractBag(path, deposit.getMimeType(), depositor.getFilepathMapping());

        var bagDir = bagExtractor.getBagDir(path);
        log.info("Bag dir found, it is named {}", bagDir);

        deposit.setState(DepositState.SUBMITTED);
        deposit.setStateDescription("Deposit is valid and ready for post-submission processing");
        deposit.setBagName(bagDir.getFileName().toString());
        deposit.setMimeType(null);

        var metadata = bagItManager.getBagItMetaData(path.resolve(deposit.getBagName()), depositId);
        deposit.setSwordToken(metadata.getSwordToken());
        deposit.setOtherId(metadata.getOtherId());
        deposit.setOtherIdVersion(metadata.getOtherIdVersion());

        depositPropertiesManager.saveProperties(path, deposit);

        removeZipFiles(path);

        updateManifests(path);

        var collection = collectionManager.getCollectionByName(deposit.getCollectionId());
        var targetPath = getDepositPath(collection, depositId);
        fileService.move(path, targetPath);

        return deposit;
    }

    private void updateManifests(Path path) {

    }

    private Stream<Path> getDepositFiles(Path path) throws IOException {
        return fileService.listFiles(path).filter(f -> !f.getFileName().equals(Path.of("deposit.properties")));
    }

    private void removeZipFiles(Path path) throws IOException {
        var files = getDepositFiles(path).collect(Collectors.toList());

        for (var file : files) {
            try {
                fileService.deleteFile(file);
            }
            catch (IOException e) {
                log.warn("Unable to remove file {}", file, e);
            }
        }
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
        return collectionConfig.getDeposits().resolve(id);
    }

    private Path getUploadPath(CollectionConfig collectionConfig, String id) {
        return collectionConfig.getUploads().resolve(id);
    }

}
