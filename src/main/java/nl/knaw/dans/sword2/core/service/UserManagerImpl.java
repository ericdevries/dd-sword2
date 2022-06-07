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
package nl.knaw.dans.sword2.core.service;

import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.core.config.UserConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UserManagerImpl implements UserManager {
    private final Map<String, Depositor> depositorMap = new HashMap<>();

    public UserManagerImpl(List<UserConfig> users) {
        for (var user: users) {
            var depositor = new Depositor(user.getName(), user.getFilepathMapping(), Set.copyOf(user.getCollections()));
            depositorMap.put(user.getName(), depositor);
        }
    }

    @Override
    public Depositor getDepositorById(String id) {
        return depositorMap.get(id);
    }
}
