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
package nl.knaw.dans.sword2.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.dans.sword2.core.config.UserConfig;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SwordAuthenticator implements Authenticator<BasicCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(SwordAuthenticator.class);

    private final List<UserConfig> userList;
    private final HttpClient httpClient;

    public SwordAuthenticator(List<UserConfig> userList, HttpClient httpClient) {
        this.userList = userList;
        this.httpClient = httpClient;
    }

    @Override
    public Optional<Depositor> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (userList.size() == 0) {
            throw new AuthenticationException("No users available");
        }

        for (var user : userList) {
            if (user.getName().equals(credentials.getUsername())) {
                log.debug("Authenticating user {}", credentials.getUsername());

                if (user.getPasswordDelegate() != null) {
                    log.debug("Using delegate {} to authenticate user {}", user.getPasswordDelegate(), user.getName());
                    if (validatePasswordWithDelegate(credentials, user.getPasswordDelegate())) {
                        return Optional.of(new Depositor(user.getName(), user.getFilepathMapping(), Set.copyOf(user.getCollections())));
                    }
                }
                else if (user.getPasswordHash() != null) {
                    log.debug("Using password hash to authenticate user {}", user.getName());
                    if (BCrypt.checkpw(credentials.getPassword(), user.getPasswordHash())) {
                        return Optional.of(new Depositor(user.getName(), user.getFilepathMapping(), Set.copyOf(user.getCollections())));
                    }
                }
            }
        }

        log.debug("No matching users found for provided credentials with username {}", credentials.getUsername());
        return Optional.empty();
    }

    boolean validatePasswordWithDelegate(BasicCredentials basicCredentials, URL passwordDelegate) throws AuthenticationException {
        try {
            var auth = basicCredentials.getUsername() + ":" + basicCredentials.getPassword();
            var encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            var header = String.format("Basic %s", new String(encodedAuth));

            var post = new HttpPost(passwordDelegate.toURI());
            post.setHeader("Authorization", header);

            var response = httpClient.execute(post);

            return response.getStatusLine().getStatusCode() == 204;
        }
        catch (URISyntaxException | IOException e) {
            throw new AuthenticationException("Unable to perform authentication check with delegate", e);
        }
    }
}
