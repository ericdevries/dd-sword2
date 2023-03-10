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
import nl.knaw.dans.sword2.core.config.PasswordDelegateConfig;
import nl.knaw.dans.sword2.core.config.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedHashMap;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwordAuthenticatorTest {

    private final AuthenticationService authenticationService = Mockito.mock(AuthenticationService.class);
    private final URL passwordDelegate = new URL("http://test.com/");

    SwordAuthenticatorTest() throws MalformedURLException {
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(authenticationService);
    }

    SwordAuthenticator getAuthenticator(List<UserConfig> users) {
        var delegate = new PasswordDelegateConfig();
        delegate.setForwardHeaders(List.of("x-dataverse-key", "authorization"));
        delegate.setUrl(passwordDelegate);

        var config = new AuthorizationConfig();
        config.setUsers(users);
        config.setPasswordDelegateConfig(delegate);

        return new SwordAuthenticator(config, authenticationService);
    }

    HeaderCredentials buildCredentials(String username, String password, String header) {
        var headers = new MultivaluedHashMap<String, String>();

        if (header != null) {
            headers.put("x-dataverse-key", List.of(header));
        }

        if (username != null) {
            var formatted = String.format("%s:%s", username, password);
            var pass = Base64.getEncoder().encodeToString(formatted.getBytes());
            headers.putSingle("Authorization", "Basic " + pass);
        }

        return new HeaderCredentials(headers);
    }

    @Test
    void authenticate_should_return_empty_optional_if_no_users_are_configured() {
        var result = assertDoesNotThrow(() ->
            getAuthenticator(List.of()).authenticate(
                buildCredentials("user", "password", null)
            ));

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_should_return_empty_optional_if_password_is_incorrect() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>()));

        assertTrue(getAuthenticator(userList).authenticate(
            buildCredentials("user001", "different_password", null)
        ).isEmpty());
    }

    @Test
    void authenticate_should_return_user_if_username_and_password_are_correct() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>()));

        assertEquals("user001", getAuthenticator(userList).authenticate(
            buildCredentials("user001", "password", null)
        ).get().getName());
    }

    @Test
    void authenticate_should_call_delegate_http_service_if_config_says_so() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.when(authenticationService.authenticateWithHeaders(Mockito.any()))
            .thenReturn(Optional.of("user001"));

        assertEquals("user001", getAuthenticator(userList).authenticate(
            buildCredentials("user001", "password", null)
        ).get().getName());
    }

    @Test
    void authenticate_should_return_empty_optional_if_delegate_returns_401_unauthorized() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.when(authenticationService.authenticateWithHeaders(Mockito.any()))
            .thenReturn(Optional.empty());

        assertTrue(getAuthenticator(userList)
            .authenticate(
                buildCredentials("user001", "password", null)
            ).isEmpty());

    }

    @Test
    void authenticate_should_propagate_AuthenticationException() throws AuthenticationException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        Mockito.doThrow(AuthenticationException.class)
            .when(authenticationService).authenticateWithHeaders(Mockito.any());

        assertThrows(AuthenticationException.class, () -> getAuthenticator(userList)
            .authenticate(
                buildCredentials("user001", "password", null)
            )
        );
    }
}
