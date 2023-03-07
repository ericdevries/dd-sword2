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

import io.dropwizard.auth.basic.BasicCredentials;

import javax.ws.rs.core.MultivaluedMap;

public class CombinedCredentials {
    private BasicCredentials basicCredentials;
    private MultivaluedMap<String, String> headers;

    public CombinedCredentials() {

    }

    public CombinedCredentials(BasicCredentials basicCredentials, MultivaluedMap<String, String> headers) {
        this.basicCredentials = basicCredentials;
    }

    public BasicCredentials getBasicCredentials() {
        return basicCredentials;
    }

    public void setBasicCredentials(BasicCredentials basicCredentials) {
        this.basicCredentials = basicCredentials;
    }

    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultivaluedMap<String, String> headers) {
        this.headers = headers;
    }
}
