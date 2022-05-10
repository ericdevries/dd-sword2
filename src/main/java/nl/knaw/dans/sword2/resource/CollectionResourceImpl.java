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
import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.exceptions.InvalidHeaderException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.models.error.Generator;
import nl.knaw.dans.sword2.models.statement.Feed;
import nl.knaw.dans.sword2.models.statement.FeedEntry;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositReceiptFactory;
import nl.knaw.dans.sword2.service.ErrorResponseFactory;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public class CollectionResourceImpl extends BaseHandler implements CollectionResource {

    private final DepositHandler depositHandler;
    private final DepositReceiptFactory depositReceiptFactory;

    public CollectionResourceImpl(DepositHandler depositHandler, DepositReceiptFactory depositReceiptFactory, ErrorResponseFactory errorResponseFactory) {
        super(errorResponseFactory);
        this.depositHandler = depositHandler;
        this.depositReceiptFactory = depositReceiptFactory;
    }

    @Override
    public Feed getCollection(HttpHeaders headers, Depositor depositor) {
        var feed = new Feed();
        feed.setGenerator(new Generator(URI.create("http://www.swordapp.org/"), "2.0"));
        feed.setEntries(List.of(new FeedEntry()));

        return feed;
    }

    @Override
    public Response depositMultipart(MultiPart multiPart, String collectionId, HttpHeaders headers, Depositor depositor) {

        String filename = null;
        String hash = null;
        String packaging = null;
        long contentLength = -1;
        String mimeType = null;
        InputStream inputStream = null;

        try {
            var inProgress = getInProgress(headers.getHeaderString("in-progress"));

            for (var part : multiPart.getBodyParts()) {
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

                var deposit = depositHandler.createDepositWithPayload(collectionId, depositor, inProgress, MediaType.valueOf(mimeType), hash, packaging, filename, contentLength, inputStream);

                var entry = depositReceiptFactory.createDepositReceipt(deposit);

                return Response.status(Response.Status.CREATED)
                    .header("Last-Modified", formatDateTime(deposit.getCreated()))
                    .header("Content-MD5", "")
                    .header("Location", depositReceiptFactory.getDepositLocation(deposit))
                    .entity(entry).build();
            }
        }
        catch (IOException | InvalidHeaderException | InvalidDepositException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_BAD_REQUEST, e.getMessage());
        }
        catch (CollectionNotFoundException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_METHOD_NOT_ALLOWED, e.getMessage());
        }
        catch (HashMismatchException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_CHECKSUM_MISMATCH);
        }
        catch (NotEnoughDiskSpaceException e) {
            throw new WebApplicationException(503);
        }

        return buildSwordErrorResponse(UriRegistry.ERROR_BAD_REQUEST, "Attempting to store and check deposit which has no input stream");
    }

    @Override
    public Response depositAtom(String collectionId, HttpHeaders headers, Depositor depositor) {
        return null;
    }

    @Override
    public Response depositAnything(InputStream inputStream, String collectionId, HttpHeaders headers, Depositor depositor) {

        try {
            var contentType = getContentType(headers.getHeaderString("content-type"));
            var inProgress = getInProgress(headers.getHeaderString("in-progress"));

            var contentDisposition = headers.getHeaderString("content-disposition");
            var md5 = headers.getHeaderString("content-md5");
            var packaging = getPackaging(headers.getHeaderString("packaging"));

            var filename = getFilenameFromContentDisposition(contentDisposition, "filename");
            var filesize = getContentLength(headers.getHeaderString("content-length"));

            var deposit = depositHandler.createDepositWithPayload(collectionId, depositor, inProgress, contentType, md5, packaging, filename, filesize, inputStream);

            var entry = depositReceiptFactory.createDepositReceipt(deposit);

            return Response.status(Response.Status.CREATED)
                .header("Last-Modified", formatDateTime(deposit.getCreated()))
                .header("Location", depositReceiptFactory.getDepositLocation(deposit))
                .entity(entry)
                .build();

        }
        catch (IOException | InvalidHeaderException | InvalidDepositException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_BAD_REQUEST, e.getMessage());
        }
        catch (CollectionNotFoundException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_METHOD_NOT_ALLOWED, e.getMessage());
        }
        catch (HashMismatchException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_CHECKSUM_MISMATCH);
        }
        catch (NotEnoughDiskSpaceException e) {
            throw new WebApplicationException(503);
        }
    }
}
