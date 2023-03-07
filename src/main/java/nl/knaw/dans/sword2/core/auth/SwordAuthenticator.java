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
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.dans.sword2.core.config.AuthorizationConfig;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

public class SwordAuthenticator implements Authenticator<BasicCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(SwordAuthenticator.class);

    private final AuthorizationConfig authorizationConfig;

    private final DataverseAuthenticationService dataverseAuthenticationService;

    public SwordAuthenticator(AuthorizationConfig authorizationConfig, DataverseAuthenticationService dataverseAuthenticationService) {
        this.authorizationConfig = authorizationConfig;
        this.dataverseAuthenticationService = dataverseAuthenticationService;
    }

    @Override
    public Optional<Depositor> authenticate(BasicCredentials credentials) throws AuthenticationException {
        var userList = authorizationConfig.getUsers();
        var passwordDelegate = authorizationConfig.getPasswordDelegate();

        var user = userList.stream()
            .filter(u -> u.getName().equals(credentials.getUsername()))
            .findFirst();

        if (user.isEmpty()) {
            log.debug("No matching users found for provided credentials with username {}", credentials.getUsername());
            return Optional.empty();
        }

        var userConfig = user.get();

        log.debug("Authenticating user {}", credentials.getUsername());

        if (userConfig.getPasswordHash() != null) {
            log.debug("Using password hash to authenticate user {}", userConfig.getName());
            if (BCrypt.checkpw(credentials.getPassword(), userConfig.getPasswordHash())) {
                return Optional.of(new Depositor(userConfig.getName(), userConfig.getFilepathMapping(), Set.copyOf(userConfig.getCollections())));
            }
        }
        else if (passwordDelegate != null) {
            log.debug("Using delegate {} to authenticate user {}", passwordDelegate, userConfig.getName());
            if (validatePasswordWithDelegate(credentials)) {
                return Optional.of(new Depositor(userConfig.getName(), userConfig.getFilepathMapping(), Set.copyOf(userConfig.getCollections())));
            }
        }
        else {
            log.warn("No valid authentication mechanism configured for user {}", userConfig.getName());
        }

        return Optional.empty();
    }

    boolean validatePasswordWithDelegate(BasicCredentials basicCredentials) throws AuthenticationException {
        return dataverseAuthenticationService.authenticateWithBasic(basicCredentials).isPresent();
    }
}
