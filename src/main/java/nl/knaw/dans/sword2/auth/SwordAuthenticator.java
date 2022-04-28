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
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.dans.sword2.config.UserConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SwordAuthenticator implements Authenticator<BasicCredentials, Depositor> {

    private final List<UserConfig> userList;

    public SwordAuthenticator(List<UserConfig> userList) {
        this.userList = userList;
    }

    @Override
    public Optional<Depositor> authenticate(BasicCredentials credentials) throws AuthenticationException {
        if (userList.size() == 0) {
            throw new AuthenticationException("No users available");
        }

        for (var user : userList) {
            if (user.getName().equals(credentials.getUsername())) {
                if (BCrypt.checkpw(credentials.getPassword(), user.getPasswordHash())) {
                    return Optional.of(new Depositor(user.getName(), user.getFilepathMapping(), Set.copyOf(user.getCollections())));
                }
            }
        }

        return Optional.empty();
    }
}
