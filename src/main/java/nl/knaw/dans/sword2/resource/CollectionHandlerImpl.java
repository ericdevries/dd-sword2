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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;
import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.UriRegistry;
import nl.knaw.dans.sword2.exceptions.HashMismatchException;
import nl.knaw.dans.sword2.exceptions.InvalidContentDispositionException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.models.entry.Entry;
import nl.knaw.dans.sword2.service.ChecksumCalculator;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositReceiptFactory;
import org.apache.commons.fileupload.ParameterParser;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Singleton
public class CollectionHandlerImpl implements CollectionHandler {

    private final DepositHandler depositHandler;
    private final DepositReceiptFactory depositReceiptFactory;

    private final ChecksumCalculator checksumCalculator;

    @Inject
    public CollectionHandlerImpl(DepositHandler depositHandler,
        DepositReceiptFactory depositReceiptFactory,
        ChecksumCalculator checksumCalculator
    ) {
        this.depositHandler = depositHandler;
        this.depositReceiptFactory = depositReceiptFactory;
        this.checksumCalculator = checksumCalculator;
    }

    @Override
    public Entry getDeposit(HttpHeaders headers) {
        var element = new Entry();
        element.setId("TESTID");
        element.setTitle("TITLE");
        return element;
    }

    @Override
    public Response depositMultipart(FormDataMultiPart formDataMultiPart,
        String collectionId,
        HttpHeaders headers
    ) {
        return null;
    }

    @Override
    public Response depositAtom(String collectionId, HttpHeaders headers) {
        return null;
    }


    @Override
    public Response depositAnything(InputStream inputStream,
        String collectionId,
        HttpHeaders headers
    ) {
        var contentType = getContentType(headers.getHeaderString("content-type"));
        var inProgress = getInProgress(headers.getHeaderString("in-progress"));

        var contentDisposition = headers.getHeaderString("content-disposition");
        var md5 = headers.getHeaderString("content-md5");
        var packaging = getPackaging(headers.getHeaderString("packaging"));

        var filename = getFilenameFromContentDisposition(contentDisposition, "filename");
        var filesize = getContentLength(headers.getHeaderString("content-length"));

        var deposit = new Deposit();
        deposit.setFilename(filename);
        deposit.setMd5(md5);
        deposit.setPackaging(packaging);
        deposit.setMimeType(contentType.toString());
        deposit.setContentLength(filesize);
        deposit.setId(UUID.randomUUID()
            .toString());
        deposit.setInProgress(inProgress);

        try {
            var payloadPath = depositHandler.storeDepositContent(deposit, inputStream);
            var properties = depositHandler.createDeposit(deposit, payloadPath);
            var entry = depositReceiptFactory.createDepositReceipt(deposit, properties);

            return Response.status(Response.Status.CREATED)
                .entity(entry)
                .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        catch (NotEnoughDiskSpaceException e) {
            e.printStackTrace();
        }
        catch (HashMismatchException e) {
            e.printStackTrace();
        }
        catch (InvalidContentDispositionException e) {
            e.printStackTrace();
        }

        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .build();

    }

    String generateMD5Hash(Path path) {
        try {
            var md = MessageDigest.getInstance("MD5");
            var is = Files.newInputStream(path);
            var buf = new byte[1024 * 8];

            var bytesRead = 0;

            while ((bytesRead = is.read(buf)) != -1) {
                md.update(buf, 0, bytesRead);
            }

            return DatatypeConverter.printHexBinary(md.digest())
                .toLowerCase(Locale.ROOT);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    String getFilenameFromContentDisposition(String contentDisposition, String key) {
        if (contentDisposition == null || key == null) {
            return null;
        }

        var parameterParser = new ParameterParser();
        var parameters = parameterParser.parse(contentDisposition, ';');

        return parameters.get(key);
    }

    String getPackaging(String header) {
        if (header == null) {
            return UriRegistry.PACKAGE_BINARY;
        }

        return header;
    }

    long getContentLength(String header) {
        try {
            if (header != null) {
                return Long.parseLong(header);
            }
        } catch (NumberFormatException ignored) {

        }

        return -1L;
    }

    private boolean getInProgress(String header) {
        if (header == null) {
            return false;
        }

        if ("true".equals(header)) {
            return true;
        } else if ("false".equals(header)) {
            return false;
        } else {
            // TODO throw some exception
            return false;
        }
    }

    MediaType getContentType(String contentType) {
        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_TYPE;
        }

        return MediaType.valueOf(contentType);
    }

    private String decodeURL(String url) {
        if (url == null) {
            return null;
        }

        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }
}
