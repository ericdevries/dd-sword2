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

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import nl.knaw.dans.sword2.core.config.AuthorizationConfig;
import nl.knaw.dans.sword2.core.config.UserConfig;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SwordAuthenticator implements Authenticator<HeaderCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(SwordAuthenticator.class);

    private final AuthorizationConfig authorizationConfig;

    private final AuthenticationService authenticationService;

    public SwordAuthenticator(AuthorizationConfig authorizationConfig, AuthenticationService authenticationService) {
        this.authorizationConfig = authorizationConfig;
        this.authenticationService = authenticationService;
    }

    @Override
    public Optional<Depositor> authenticate(HeaderCredentials credentials) throws AuthenticationException {
        var basicCredentials = credentials.getBasicCredentials();
        // if basic credentials are provided, check if we know this user
        if (basicCredentials != null) {
            var user = getUserByName(basicCredentials.getUsername());

            if (user.isEmpty()) {
                log.debug("No matching users found for provided credentials with username {}", basicCredentials.getUsername());
                return Optional.empty();
            }

            // the user is found, now check if we should do a local password check
            var userConfig = user.get();

            if (userConfig.getPasswordHash() != null) {
                log.debug("User is configured with a password hash, validating password for user {}", userConfig.getName());

                // always return a value if there is a password hash, even if it does not match
                if (BCrypt.checkpw(basicCredentials.getPassword(), userConfig.getPasswordHash())) {
                    return Optional.of(new Depositor(userConfig.getName(), userConfig.getFilepathMapping(), Set.copyOf(userConfig.getCollections())));
                }
                else {
                    return Optional.empty();
                }
            }
        }

        log.debug("No basic credentials provided, or not configured with a local password; forwarding request to passwordDelegate");

        // no basic credentials, or user was not configured with a password hash, forward request to password delegate
        return authenticationService.authenticateWithHeaders(credentials.getHeaders())
            .map(this::getUserByName)
            .flatMap(f -> f)
            .map(u -> new Depositor(u.getName(), u.getFilepathMapping(), new HashSet<>(u.getCollections())));
    }

    Optional<UserConfig> getUserByName(String name) {
        return authorizationConfig.getUsers().stream()
            .filter(u -> u.getName().equals(name))
            .findFirst();
    }
}
