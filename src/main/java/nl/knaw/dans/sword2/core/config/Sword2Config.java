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

import nl.knaw.dans.lib.util.ExecutorServiceFactory;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class Sword2Config {

    @Valid
    @NotNull
    private URI baseUrl;
    @Email
    @NotEmpty
    private String emailAddress;
    @NotEmpty
    private List<@Valid  CollectionConfig> collections;
    @Valid
    @NotNull
    private Duration rescheduleDelay;
    @Valid
    @NotNull
    private ExecutorServiceFactory finalizingQueue;
    @Valid
    @NotNull
    private ExecutorServiceFactory rescheduleQueue;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public ExecutorServiceFactory getRescheduleQueue() {
        return rescheduleQueue;
    }

    public void setRescheduleQueue(ExecutorServiceFactory rescheduleQueue) {
        this.rescheduleQueue = rescheduleQueue;
    }

    public URI getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<CollectionConfig> getCollections() {
        return collections;
    }

    public void setCollections(List<CollectionConfig> collections) {
        this.collections = collections;
    }

    public Duration getRescheduleDelay() {
        return rescheduleDelay;
    }

    public void setRescheduleDelay(Duration rescheduleDelay) {
        this.rescheduleDelay = rescheduleDelay;
    }

    public ExecutorServiceFactory getFinalizingQueue() {
        return finalizingQueue;
    }

    public void setFinalizingQueue(ExecutorServiceFactory finalizingQueue) {
        this.finalizingQueue = finalizingQueue;
    }

    @Override
    public String toString() {
        return "Sword2Config{" +
            "baseUrl=" + baseUrl +
            ", collections=" + collections +
            ", rescheduleDelay=" + rescheduleDelay +
            ", finalizingQueue=" + finalizingQueue +
            '}';
    }
}


