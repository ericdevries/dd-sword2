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
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.dans.sword2.core.auth.SwordAuthenticator;
import nl.knaw.dans.sword2.core.config.AuthorizationConfig;
import nl.knaw.dans.sword2.core.config.UserConfig;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwordAuthenticatorTest {

    private final HttpClient httpClient = Mockito.mock(HttpClient.class);
    private final URL passwordDelegate = new URL("http://test.com/");

    SwordAuthenticatorTest() throws MalformedURLException {
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(httpClient);
    }

    SwordAuthenticator getAuthenticator(List<UserConfig> users) {
        var config = new AuthorizationConfig();
        config.setUsers(users);
        config.setPasswordDelegate(passwordDelegate);

        return new SwordAuthenticator(config, httpClient);
    }

    @Test
    void authenticate_should_return_empty_optional_if_no_users_are_configured() {
        var emptyList = new ArrayList<UserConfig>();

        var result = assertDoesNotThrow(() ->
            getAuthenticator(List.of()).authenticate(new BasicCredentials("user", "password")
            ));

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticate_should_return_empty_optional_if_password_is_incorrect() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>()));

        assertTrue(getAuthenticator(userList).authenticate(new BasicCredentials("user001", "different_password")).isEmpty());
    }

    @Test
    void authenticate_should_return_user_if_username_and_password_are_correct() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>()));

        assertEquals("user001", getAuthenticator(userList).authenticate(new BasicCredentials("user001", "password")).get().getName());
    }

    @Test
    void authenticate_should_call_delegate_http_service_if_config_says_so() throws AuthenticationException, IOException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        var protocol = new ProtocolVersion("http", 1, 1);
        var status = new BasicStatusLine(protocol, 204, "No Content");
        var fakeResponse = new BasicHttpResponse(status, null, null);

        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(fakeResponse);

        assertEquals("user001", getAuthenticator(userList).authenticate(new BasicCredentials("user001", "password")).get().getName());
    }

    @Test
    void authenticate_should_return_empty_optional_if_delegate_returns_401_unauthorized() throws AuthenticationException, IOException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        var protocol = new ProtocolVersion("http", 1, 1);
        var status = new BasicStatusLine(protocol, 401, "Unauthorized");
        var fakeResponse = new BasicHttpResponse(status, null, null);

        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(fakeResponse);

        assertTrue(getAuthenticator(userList)
            .authenticate(new BasicCredentials("user001", "password")).isEmpty());
    }

    @Test
    void authenticate_should_return_empty_optional_if_delegate_returns_500_error() throws AuthenticationException, IOException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>()));

        var protocol = new ProtocolVersion("http", 1, 1);
        var status = new BasicStatusLine(protocol, 500, "Internal Server Error");
        var fakeResponse = new BasicHttpResponse(status, null, null);

        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(fakeResponse);

        assertThrows(AuthenticationException.class, () -> getAuthenticator(userList)
            .authenticate(new BasicCredentials("user001", "password")));
    }
}
