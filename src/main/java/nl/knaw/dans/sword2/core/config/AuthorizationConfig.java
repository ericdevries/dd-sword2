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
package nl.knaw.dans.sword2.core.config;

import javax.validation.Valid;
import java.net.URL;
import java.util.List;

public class AuthorizationConfig {

    @Valid
    private URL passwordDelegate;
    @Valid
    private List<UserConfig> users;

    public URL getPasswordDelegate() {
        return passwordDelegate;
    }

    public void setPasswordDelegate(URL passwordDelegate) {
        this.passwordDelegate = passwordDelegate;
    }

    public List<UserConfig> getUsers() {
        return users;
    }

    public void setUsers(List<UserConfig> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return "AuthorizationConfig{" +
            "passwordDelegate=" + passwordDelegate +
            ", users=" + users +
            '}';
    }
}
