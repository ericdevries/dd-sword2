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
import io.dropwizard.auth.Authenticator;
import nl.knaw.dans.sword2.core.config.AuthorizationConfig;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Optional;

public class HeaderAuthenticator implements Authenticator<HeaderCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticator.class);

    private final AuthorizationConfig authorizationConfig;
    private final HttpClient httpClient;

    private final ObjectMapper objectMapper;

    public HeaderAuthenticator(AuthorizationConfig authorizationConfig, HttpClient httpClient, ObjectMapper objectMapper) {
        this.authorizationConfig = authorizationConfig;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Depositor> authenticate(HeaderCredentials credentials) throws AuthenticationException {
        if (authorizationConfig.getPasswordDelegate() == null) {
            return Optional.empty();
        }

        var userName = getUserNameFromToken(credentials);

        return userName.flatMap(s -> authorizationConfig.getUsers().stream()
            .filter(m -> m.getName().equals(s))
            .map(m -> new Depositor(m.getName(), m.getFilepathMapping(), new HashSet<>(m.getCollections())))
            .findFirst());

    }

    Optional<String> getUserNameFromToken(HeaderCredentials credentials) throws AuthenticationException {
        return getAuthenticatedResponse(credentials)
            .map((m) -> {
                try {
                    var tree = objectMapper.readTree(m.getEntity().getContent());
                    return tree.get("userId").asText();
                }
                catch (Exception e) {
                    log.error("Error parsing JSON", e);
                }

                return null;
            });
    }

    Optional<HttpResponse> getAuthenticatedResponse(HeaderCredentials credentials) throws AuthenticationException {
        try {
            var post = new HttpPost(authorizationConfig.getPasswordDelegate().toURI());
            post.setHeader("X-Dataverse-Key", credentials.getValue());

            var response = httpClient.execute(post);
            var status = response.getStatusLine().getStatusCode();
            log.debug("Delegate returned status code {}", status);

            switch (status) {
                case 200:
                    return Optional.of(response);
                case 401:
                    return Optional.empty();
                default:
                    throw new AuthenticationException(String.format(
                        "Unexpected status code returned: %s (message: %s)", status, response.getStatusLine().getReasonPhrase()
                    ));
            }
        }
        catch (URISyntaxException | IOException e) {
            throw new AuthenticationException("Unable to validate credentials", e);
        }
    }
}
