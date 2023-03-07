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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.AuthenticationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final URL passwordDelegate;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AuthenticationServiceImpl(URL passwordDelegate, HttpClient httpClient, ObjectMapper objectMapper) {
        this.passwordDelegate = passwordDelegate;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<String> authenticateWithHeaders(MultivaluedMap<String, String> headers) throws AuthenticationException {
        try {
            var request = new HttpPost(passwordDelegate.toURI());

            for (var entry : headers.entrySet()) {
                if (entry.getValue().size() > 0) {
                    request.setHeader(entry.getKey(), entry.getValue().get(0));
                }
            }

            return doRequest(request);
        }
        catch (URISyntaxException | IOException e) {
            throw new AuthenticationException("Unable to validate credentials", e);
        }
    }

    private Optional<String> doRequest(HttpUriRequest request) throws AuthenticationException, IOException {
        var response = httpClient.execute(request);
        var status = response.getStatusLine().getStatusCode();
        log.debug("Delegate returned status code {}", status);

        switch (status) {
            case 200:
                return getUsernameFromResponse(response);
            case 401:
                return Optional.empty();
            default:
                throw new AuthenticationException(String.format(
                    "Unexpected status code returned: %s (message: %s)", status, response.getStatusLine().getReasonPhrase()
                ));
        }
    }

    private Optional<String> getUsernameFromResponse(HttpResponse response) {
        try {
            var tree = objectMapper.readTree(response.getEntity().getContent());
            return Optional.ofNullable(tree.get("userId").asText());
        }
        catch (Exception e) {
            log.error("Error parsing JSON", e);
        }

        return Optional.empty();
    }
}
