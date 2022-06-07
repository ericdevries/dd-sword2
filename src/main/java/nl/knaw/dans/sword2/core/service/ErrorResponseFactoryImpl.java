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
package nl.knaw.dans.sword2.core.service;

import nl.knaw.dans.sword2.core.config.UriRegistry;
import nl.knaw.dans.sword2.api.error.Error;
import nl.knaw.dans.sword2.api.error.Generator;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ErrorResponseFactoryImpl implements ErrorResponseFactory {
    private final DateTimeFormatter errorDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final Map<String, Integer> errorMap;

    public ErrorResponseFactoryImpl() {
        this.errorMap = new HashMap<>();

        // set up the error codes mapping (copied from easy-sword2-lib)
        errorMap.put(UriRegistry.ERROR_BAD_REQUEST, 400); // bad request
        errorMap.put(UriRegistry.ERROR_CHECKSUM_MISMATCH, 412); // precondition failed
        errorMap.put(UriRegistry.ERROR_CONTENT, 415); // unsupported media type
        errorMap.put(UriRegistry.ERROR_MEDIATION_NOT_ALLOWED, 412); // precondition failed
        errorMap.put(UriRegistry.ERROR_METHOD_NOT_ALLOWED, 405); // method not allowed
        errorMap.put(UriRegistry.ERROR_TARGET_OWNER_UNKNOWN, 403); // forbidden
        errorMap.put(UriRegistry.ERROR_MAX_UPLOAD_SIZE_EXCEEDED, 413); // forbidden

    }

    @Override
    public ErrorResponse buildSwordErrorResponse(String errorCode) {
        return buildSwordErrorResponse(errorCode, null);
    }

    @Override
    public ErrorResponse buildSwordErrorResponse(String errorCode, String errorMessage) {
        var errorDocument = new Error();
        errorDocument.setTitle("ERROR");
        errorDocument.setTreatment("Processing failed");
        errorDocument.setGenerator(new Generator(URI.create("http://www.swordapp.org/"), "2.0"));
        errorDocument.setSummary(errorMessage == null ? errorCode : errorMessage);
        errorDocument.setUpdated(OffsetDateTime.now().format(errorDateTimeFormatter));

        var statusCode = errorMap.getOrDefault(errorCode, 400);

        return new ErrorResponse(errorDocument, statusCode);
    }
}
