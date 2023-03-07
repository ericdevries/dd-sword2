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
import nl.knaw.dans.sword2.core.config.AuthorizationConfig;
import nl.knaw.dans.sword2.core.config.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeaderAuthenticatorTest {

    private final DataverseAuthenticationService authenticationService = Mockito.mock(DataverseAuthenticationService.class);
    private final URL passwordDelegate = new URL("http://test.com/");

    HeaderAuthenticatorTest() throws MalformedURLException {
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(authenticationService);
    }

    HeaderAuthenticator getAuthenticator(List<UserConfig> users) {
        var config = new AuthorizationConfig();
        config.setUsers(users);
        config.setPasswordDelegate(passwordDelegate);

        return new HeaderAuthenticator(config, authenticationService);
    }

    @Test
    void authenticate_should_return_empty_optional_if_no_users_are_configured() {
        var emptyList = new ArrayList<UserConfig>();

        var result = assertDoesNotThrow(() ->
            getAuthenticator(List.of()).authenticate(new HeaderCredentials("token"))
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_should_call_delegate_http_service() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.when(authenticationService.authenticateWithHeader(Mockito.any()))
            .thenReturn(Optional.of("user001"));

        assertEquals("user001", getAuthenticator(userList).authenticate(new HeaderCredentials("token")).get().getName());
    }

    @Test
    void authenticate_should_return_empty_optional_if_delegate_returns_401_unauthorized() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.when(authenticationService.authenticateWithHeader(Mockito.any()))
            .thenReturn(Optional.empty());

        assertTrue(getAuthenticator(userList)
            .authenticate(new HeaderCredentials("token")).isEmpty());
    }

    @Test
    void authenticate_should_return_empty_optional_if_users_do_not_match() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.when(authenticationService.authenticateWithHeader(Mockito.any()))
            .thenReturn(Optional.of("different_user"));

        assertTrue(getAuthenticator(userList)
            .authenticate(new HeaderCredentials("token")).isEmpty());
    }

    @Test
    void authenticate_should_propagate_AuthenticationException() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.doThrow(AuthenticationException.class)
            .when(authenticationService).authenticateWithHeader(Mockito.any());

        assertThrows(AuthenticationException.class, () -> getAuthenticator(userList)
            .authenticate(new HeaderCredentials("token")));
    }
}