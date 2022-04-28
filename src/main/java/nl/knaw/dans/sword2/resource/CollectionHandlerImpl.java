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

import nl.knaw.dans.sword2.UriRegistry;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.exceptions.HashMismatchException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.models.entry.Entry;
import nl.knaw.dans.sword2.service.ChecksumCalculator;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositReceiptFactory;
import org.apache.commons.fileupload.ParameterParser;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;

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
    public Response depositMultipart(MultiPart multiPart,
        String collectionId,
        HttpHeaders headers,
        Depositor depositor
    ) {

        String filename = null;
        String hash = null;
        String packaging = null;
        long contentLength = -1;
        String mimeType = null;
        InputStream inputStream = null;

        var inProgress = getInProgress(headers.getHeaderString("in-progress"));

        for (var part : multiPart.getBodyParts()) {
            System.out.println("PART :" + part);
            var name = part.getContentDisposition().getParameters().get("name");


            if ("atom".equals(name)) {
                // TODO get atom part
//                InputStream entryPart = item.getInputStream();
//                Abdera abdera = new Abdera();
//                Parser parser = abdera.getParser();
//                Document<Entry> entryDoc = parser.parse(entryPart);
//                Entry entry = entryDoc.getRoot();
//                deposit.setEntry(entry);
            }
            else if ("payload".equals(name)) {
                filename = part.getContentDisposition().getFileName();
                hash = part.getHeaders().getFirst("content-md5");
                packaging = part.getHeaders().getFirst("packaging");
                contentLength = part.getContentDisposition().getSize();
                mimeType = "application/octet-stream";
                inputStream = part.getEntityAs(InputStream.class);

                if (part.getMediaType() != null) {
                    mimeType = part.getMediaType().toString().split(";")[0];
                }
            }
        }

        try {
            var deposit = depositHandler.createDepositWithPayload(
                collectionId, depositor, inProgress, MediaType.valueOf(mimeType), hash, packaging, filename, contentLength, inputStream);

            var entry = depositReceiptFactory.createDepositReceipt(deposit);

            return Response.status(Response.Status.CREATED)
                .entity(entry)
                .build();

        }
        catch (CollectionNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NotEnoughDiskSpaceException e) {
            e.printStackTrace();
        }
        catch (HashMismatchException e) {
            e.printStackTrace();
        }


        System.out.println("MULTIPART: " + multiPart);
        return null;
    }

    @Override
    public Response depositAtom(String collectionId, HttpHeaders headers, Depositor depositor) {
        return null;
    }

    @Override
    public Response depositAnything(InputStream inputStream,
        String collectionId,
        HttpHeaders headers,
        Depositor depositor
    ) {

        var contentType = getContentType(headers.getHeaderString("content-type"));
        var inProgress = getInProgress(headers.getHeaderString("in-progress"));

        var contentDisposition = headers.getHeaderString("content-disposition");
        var md5 = headers.getHeaderString("content-md5");
        var packaging = getPackaging(headers.getHeaderString("packaging"));

        var filename = getFilenameFromContentDisposition(contentDisposition, "filename");
        var filesize = getContentLength(headers.getHeaderString("content-length"));

        try {
            var deposit = depositHandler.createDepositWithPayload(
                collectionId, depositor, inProgress, contentType, md5, packaging, filename, filesize, inputStream);

            var entry = depositReceiptFactory.createDepositReceipt(deposit);

            return Response.status(Response.Status.CREATED)
                .entity(entry)
                .build();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (NotEnoughDiskSpaceException e) {
            throw new WebApplicationException(503);
        }
        catch (CollectionNotFoundException e) {
            e.printStackTrace();
        }
        catch (HashMismatchException e) {
            e.printStackTrace();
        }
        return Response.status(Status.INTERNAL_SERVER_ERROR)
            .build();

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
        }
        catch (NumberFormatException ignored) {

        }

        return -1L;
    }

    private boolean getInProgress(String header) {
        if (header == null) {
            return false;
        }

        if ("true".equals(header)) {
            return true;
        }
        else if ("false".equals(header)) {
            return false;
        }
        else {
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

}
