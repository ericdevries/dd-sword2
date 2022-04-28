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

import java.security.Principal;
import java.util.Set;

public class Depositor implements Principal {
    private String name;
    private Boolean filepathMapping;
    private Set<String> collections;

    public Depositor(String name, Boolean filepathMapping, Set<String> collections) {
        this.name = name;
        this.filepathMapping = filepathMapping;
        this.collections = collections;
    }

    public Depositor() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getFilepathMapping() {
        return filepathMapping;
    }

    public void setFilepathMapping(Boolean filepathMapping) {
        this.filepathMapping = filepathMapping;
    }

    public Set<String> getCollections() {
        return collections;
    }

    public void setCollections(Set<String> collections) {
        this.collections = collections;
    }
}
