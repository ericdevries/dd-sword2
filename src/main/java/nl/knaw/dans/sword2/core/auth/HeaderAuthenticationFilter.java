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

import io.dropwizard.auth.AuthFilter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public class HeaderAuthenticationFilter<P extends Principal> extends AuthFilter<HeaderCredentials, P> {

    public HeaderAuthenticationFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var credentials = getCredentials(requestContext);

        // not sure what will break if we put our custom auth method in here, so lets stick with BASIC_AUTH
        if (!authenticate(requestContext, credentials, SecurityContext.BASIC_AUTH)) {
            throw unauthorizedHandler.buildException(prefix, realm);
        }
    }

    private HeaderCredentials getCredentials(ContainerRequestContext requestContext) {
        return new HeaderCredentials(requestContext.getHeaders());
    }

    public static class Builder<P extends Principal> extends
        AuthFilterBuilder<HeaderCredentials, P, HeaderAuthenticationFilter<P>> {

        @Override
        protected HeaderAuthenticationFilter<P> newInstance() {
            return new HeaderAuthenticationFilter<>();
        }
    }
}
