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

import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.UriRegistry;
import nl.knaw.dans.sword2.models.Entry;
import nl.knaw.dans.sword2.models.Link;
import nl.knaw.dans.sword2.service.DepositManager;
import org.apache.commons.fileupload.ParameterParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Singleton
public class CollectionHandlerImpl implements CollectionHandler {

    private final DepositManager depositManager;

    @Inject
    public CollectionHandlerImpl(DepositManager depositManager) {
        this.depositManager = depositManager;
    }

    @Override
    public Entry getDeposit(HttpHeaders headers) {
        var element = new Entry();
        element.setId("TESTID");
        element.setTitle("TITLE");
        return element;
    }

    private String decodeURL(String url) {
        if (url == null) {
            return null;
        }

        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }

    @Override
    public Response depositAnything(InputStream inputStream, HttpHeaders headers) {
        var contentType = getContentType(headers.getHeaderString("content-type"));
        var slug = decodeURL(headers.getHeaderString("slug"));
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
        deposit.setId(UUID.randomUUID().toString());
        deposit.setSlug(slug);
        deposit.setInProgress(inProgress);

        System.out.println("DEPOSIT: " + deposit);

        try {
            var payloadPath = depositManager.storeDepositContent(deposit, inputStream);
            depositManager.createDeposit(deposit, payloadPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        var element = new Entry();
        element.setId("TESTID");
        element.setTitle("TITLE");
        element.setPackaging(deposit.getPackaging());
        element.getLinks()
            .add(new Link(URI.create("http://www.swordserver.ac.uk/col1/mydeposit/package.zip"),
                "http://purl.org/net/sword/terms/originalDeposit", "application/zip"));

        element.getLinks()
            .addAll(List.of(new Link(URI.create("http://localhost:20312/collection/abc"), "http://purl.org/net/sword/terms/statement",
                    "application/atom+xml;type=feed"),
                new Link(URI.create("http://localhost:20312/collection/abc"), "http://purl.org/net/sword/terms/statement",
                    "application/rdf+xml")));

        return Response.status(Response.Status.CREATED)
            .entity(element)
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
        }
        catch (NoSuchAlgorithmException | IOException e) {
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
