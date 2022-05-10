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
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositReadOnlyException;
import nl.knaw.dans.sword2.exceptions.HashMismatchException;
import nl.knaw.dans.sword2.exceptions.InvalidHeaderException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositReceiptFactory;
import nl.knaw.dans.sword2.service.ErrorResponseFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ContainerHandlerImpl extends BaseHandler implements ContainerHandler {
    private final DepositReceiptFactory depositReceiptFactory;
    private final DepositHandler depositHandler;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");

    public ContainerHandlerImpl(DepositReceiptFactory depositReceiptFactory, DepositHandler depositHandler, ErrorResponseFactory errorResponseFactory) {
        super(errorResponseFactory);
        this.depositReceiptFactory = depositReceiptFactory;
        this.depositHandler = depositHandler;
    }

    @Override
    public Response getDepositReceipt(String depositId, HttpHeaders headers, Depositor depositor) {
        try {
            var deposit = depositHandler.getDeposit(depositId, depositor);
            var entry = depositReceiptFactory.createDepositReceipt(deposit);
            var location = depositReceiptFactory.getDepositLocation(deposit);

            return Response.status(Response.Status.OK)
                .header("Location", location)
                .header("Content-Type", "application/atom+xml;type=entry")
                .header("Last-Modified", OffsetDateTime.now().format(dateTimeFormatter))
                .entity(entry)
                .build();

        }
        catch (DepositNotFoundException e) {
            throw new WebApplicationException(403);
        }
    }

    @Override
    public Response getDepositReceiptHead(String depositId, HttpHeaders headers, Depositor depositor) {
        try {
            var deposit = depositHandler.getDeposit(depositId, depositor);
            var location = depositReceiptFactory.getDepositLocation(deposit);

            return Response.status(Response.Status.OK)
                .header("Location", location)
                .header("Content-Type", "application/atom+xml;type=entry")
                .header("Last-Modified", OffsetDateTime.now().format(dateTimeFormatter))
                .build();

        }
        catch (DepositNotFoundException e) {
            throw new WebApplicationException(403);
        }
    }

    @Override
    public Response addMedia(InputStream inputStream, String depositId, HttpHeaders headers, Depositor depositor) {
        try {
            var contentType = getContentType(headers.getHeaderString("content-type"));
            var inProgress = getInProgress(headers.getHeaderString("in-progress"));

            var contentDisposition = headers.getHeaderString("content-disposition");
            var md5 = headers.getHeaderString("content-md5");
            var packaging = getPackaging(headers.getHeaderString("packaging"));

            var filename = getFilenameFromContentDisposition(contentDisposition, "filename");
            var filesize = getContentLength(headers.getHeaderString("content-length"));

            var deposit = depositHandler.addPayloadToDeposit(depositId, depositor, inProgress, contentType, md5, packaging, filename, filesize, inputStream);
            var entry = depositReceiptFactory.createDepositReceipt(deposit);
            var location = depositReceiptFactory.getDepositLocation(deposit);

            return Response.status(Response.Status.OK)
                .header("Location", location)
                .header("Content-Type", "application/atom+xml;type=entry")
                .header("Last-Modified", OffsetDateTime.now().format(dateTimeFormatter))
                .entity(entry)
                .build();
        }
        catch (IOException | InvalidHeaderException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_BAD_REQUEST, e.getMessage());
        }
        catch (CollectionNotFoundException | DepositReadOnlyException e) {
            e.printStackTrace();
            return buildSwordErrorResponse(UriRegistry.ERROR_METHOD_NOT_ALLOWED, e.getMessage());
        }
        catch (HashMismatchException e) {
            return buildSwordErrorResponse(UriRegistry.ERROR_CHECKSUM_MISMATCH);
        }
        catch (NotEnoughDiskSpaceException e) {
            throw new WebApplicationException(503);
        }
        catch (DepositNotFoundException e) {
            // TODO find out how the specs deal with an unknown deposit
            throw new WebApplicationException(e, 404);
        }
    }

}
