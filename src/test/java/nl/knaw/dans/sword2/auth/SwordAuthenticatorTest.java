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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwordAuthenticatorTest {

    private final HttpClient httpClient = Mockito.mock(HttpClient.class);

    @BeforeEach
    void setUp() {
        Mockito.reset(httpClient);
    }

    @Test
    void testEmptyUserList() {
        var emptyList = new ArrayList<UserConfig>();
        assertThrows(AuthenticationException.class, () -> new SwordAuthenticator(emptyList, httpClient).authenticate(new BasicCredentials("user", "password")));
    }

    @Test
    void testReturnsEmpty() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>(), null));

        assertTrue(new SwordAuthenticator(userList, httpClient).authenticate(new BasicCredentials("user001", "different_password")).isEmpty());
    }

    @Test
    void testReturnsUser() throws AuthenticationException {
        var password = BCrypt.hashpw("password", BCrypt.gensalt());
        var userList = List.of(new UserConfig("user001", password, false, new ArrayList<>(), null));

        assertEquals("user001", new SwordAuthenticator(userList, httpClient).authenticate(new BasicCredentials("user001", "password")).get().getName());
    }

    @Test
    void testReturnsUserWithDelegate() throws AuthenticationException, IOException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>(), new URL("http://test.com/")));

        var protocol = new ProtocolVersion("http", 1, 1);
        var status = new BasicStatusLine(protocol, 204, "No Content");
        var fakeResponse = new BasicHttpResponse(status, null, null);

        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(fakeResponse);

        assertEquals("user001", new SwordAuthenticator(userList, httpClient).authenticate(new BasicCredentials("user001", "password")).get().getName());
    }

    @Test
    void testReturnsNoUserWithDelegate() throws AuthenticationException, IOException {
        var userList = List.of(new UserConfig("user001", null, false, new ArrayList<>(), new URL("http://test.com/")));

        var protocol = new ProtocolVersion("http", 1, 1);
        var status = new BasicStatusLine(protocol, 401, "Unauthorized");
        var fakeResponse = new BasicHttpResponse(status, null, null);

        Mockito.when(httpClient.execute(Mockito.any()))
            .thenReturn(fakeResponse);

        assertTrue(new SwordAuthenticator(userList, httpClient)
            .authenticate(new BasicCredentials("user001", "password")).isEmpty());
    }
}
