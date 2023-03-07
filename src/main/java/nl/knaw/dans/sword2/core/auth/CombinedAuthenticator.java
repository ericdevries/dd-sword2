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

import java.util.Optional;

public class CombinedAuthenticator implements Authenticator<CombinedCredentials, Depositor> {
    private final SwordAuthenticator swordAuthenticator;
    private final HeaderAuthenticator headerAuthenticator;

    public CombinedAuthenticator(SwordAuthenticator swordAuthenticator, HeaderAuthenticator headerAuthenticator) {
        this.swordAuthenticator = swordAuthenticator;
        this.headerAuthenticator = headerAuthenticator;
    }

    @Override
    public Optional<Depositor> authenticate(CombinedCredentials credentials) throws AuthenticationException {
        // if header credentials are provided, only use that
        if (credentials.getHeaderCredentials() != null) {
            return headerAuthenticator.authenticate(credentials.getHeaderCredentials());
        }

        // if basic credentials are provided, and header credentials are not provided, only use basic
        if (credentials.getBasicCredentials() != null) {
            return swordAuthenticator.authenticate(credentials.getBasicCredentials());
        }

        return Optional.empty();
    }

}
