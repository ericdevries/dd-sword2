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
package nl.knaw.dans.sword2.config;

import java.util.List;

public class UserConfig {

    private String name;
    private String passwordHash;
    private Boolean filepathMapping;
    private List<String> collections;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getFilepathMapping() {
        return filepathMapping;
    }

    public void setFilepathMapping(Boolean filepathMapping) {
        this.filepathMapping = filepathMapping;
    }

    public List<String> getCollections() {
        return collections;
    }

    public void setCollections(List<String> collections) {
        this.collections = collections;
    }

    @Override
    public String toString() {
        return "UserConfig{" +
            "name='" + name + '\'' +
            ", filepathMapping=" + filepathMapping +
            ", collections=" + collections +
            '}';
    }
}