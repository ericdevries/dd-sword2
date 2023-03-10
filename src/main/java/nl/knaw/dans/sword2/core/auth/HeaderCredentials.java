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
package nl.knaw.dans.sword2.core.auth;

import io.dropwizard.auth.basic.BasicCredentials;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

public class HeaderCredentials {
    private final MultivaluedMap<String, String> headers;

    public HeaderCredentials(MultivaluedMap<String, String> headers) {
        var newMap = new MultivaluedHashMap<String, String>();

        // all keys should be lower case so we can also retrieve them case-insensitive
        for (var entry : headers.entrySet()) {
            newMap.put(entry.getKey().toLowerCase(), entry.getValue());
        }

        this.headers = newMap;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    private String getHeader(String name) {
        return this.headers.getFirst(name.toLowerCase());
    }

    // Copied from io.dropwizard.auth.basic.BasicCredentialAuthFilter
    public BasicCredentials getBasicCredentials() {
        final var header = this.getHeader("Authorization");
        final var prefix = "Basic";

        if (header == null) {
            return null;
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = header.substring(0, space);
        if (!prefix.equalsIgnoreCase(method)) {
            return null;
        }

        final String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(space + 1)), StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e) {
            return null;
        }

        // Decoded credentials is 'username:password'
        final int i = decoded.indexOf(':');
        if (i <= 0) {
            return null;
        }

        final String username = decoded.substring(0, i);
        final String password = decoded.substring(i + 1);

        return new BasicCredentials(username, password);
    }

}
