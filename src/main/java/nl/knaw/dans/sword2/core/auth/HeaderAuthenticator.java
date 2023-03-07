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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Optional;

public class HeaderAuthenticator implements Authenticator<HeaderCredentials, Depositor> {

    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticator.class);

    private final AuthorizationConfig authorizationConfig;

    private final DataverseAuthenticationService dataverseAuthenticationService;

    public HeaderAuthenticator(AuthorizationConfig authorizationConfig, DataverseAuthenticationService dataverseAuthenticationService) {
        this.authorizationConfig = authorizationConfig;
        this.dataverseAuthenticationService = dataverseAuthenticationService;
    }

    @Override
    public Optional<Depositor> authenticate(HeaderCredentials credentials) throws AuthenticationException {
        if (authorizationConfig.getPasswordDelegate() == null) {
            log.warn("No password delegate configured, not proceeding");
            return Optional.empty();
        }

        var userName = dataverseAuthenticationService.authenticateWithHeader(credentials.getValue());

        return userName.flatMap(s -> authorizationConfig.getUsers().stream()
            .filter(m -> m.getName().equals(s))
            .map(m -> new Depositor(m.getName(), m.getFilepathMapping(), new HashSet<>(m.getCollections())))
            .findFirst());
    }
}
