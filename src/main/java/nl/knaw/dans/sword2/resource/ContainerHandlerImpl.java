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
import nl.knaw.dans.sword2.exceptions.InvalidContentDispositionException;
import nl.knaw.dans.sword2.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositPropertiesManager;
import nl.knaw.dans.sword2.service.DepositReceiptFactory;
import org.apache.commons.fileupload.ParameterParser;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

public class ContainerHandlerImpl implements ContainerHandler {
    private final DepositReceiptFactory depositReceiptFactory;
    private final DepositHandler depositHandler;

    public ContainerHandlerImpl(DepositReceiptFactory depositReceiptFactory, DepositHandler depositHandler) {
        this.depositReceiptFactory = depositReceiptFactory;
        this.depositHandler = depositHandler;
    }

    @Override
    public Response getDepositReceipt(String depositId, HttpHeaders headers, Depositor depositor) throws DepositNotFoundException {
        var deposit = depositHandler.getDeposit(depositId, depositor);
        var entry = depositReceiptFactory.createDepositReceipt(deposit);

        return Response.status(Response.Status.CREATED)
            .entity(entry)
            .build();
    }

    @Override
    public Response addMedia(InputStream inputStream, String depositId, HttpHeaders headers, Depositor depositor) {
        var contentType = getContentType(headers.getHeaderString("content-type"));
        var inProgress = getInProgress(headers.getHeaderString("in-progress"));

        var contentDisposition = headers.getHeaderString("content-disposition");
        var md5 = headers.getHeaderString("content-md5");
        var packaging = getPackaging(headers.getHeaderString("packaging"));

        var filename = getFilenameFromContentDisposition(contentDisposition, "filename");
        var filesize = getContentLength(headers.getHeaderString("content-length"));

        try {
            var deposit = depositHandler.addPayloadToDeposit(depositId, depositor, inProgress, contentType, md5, packaging, filename, filesize, inputStream);
            var entry = depositReceiptFactory.createDepositReceipt(deposit);

            return Response.status(Response.Status.OK)
                .entity(entry)
                .build();
        }
        catch (IOException | DepositNotFoundException | DepositReadOnlyException e) {
            e.printStackTrace();
        }
        catch (NotEnoughDiskSpaceException e) {
            e.printStackTrace();
        }
        catch (HashMismatchException e) {
            e.printStackTrace();
        }
        catch (CollectionNotFoundException e) {
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
