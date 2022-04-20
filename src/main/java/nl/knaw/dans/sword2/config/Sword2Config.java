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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import nl.knaw.dans.lib.util.ExecutorServiceFactory;
import nl.knaw.dans.sword2.config.converter.StringByteSizeConverter;

public class Sword2Config {

    @Valid
    @NotNull
    private URI baseUrl;
    @NotEmpty
    private List<CollectionConfig> collections;
    @Valid
    @NotNull
    @JsonDeserialize(converter = StringByteSizeConverter.class)
    private long diskSpaceMargin;
    @Valid
    @NotNull
    private Duration rescheduleDelay;
    @Valid
    @NotNull
    private ExecutorServiceFactory finalizingQueue;

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

    public long getDiskSpaceMargin() {
        return diskSpaceMargin;
    }

    public void setDiskSpaceMargin(long diskSpaceMargin) {
        this.diskSpaceMargin = diskSpaceMargin;
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
            ", diskSpaceMargin=" + diskSpaceMargin +
            ", rescheduleDelay=" + rescheduleDelay +
            ", finalizingQueue=" + finalizingQueue +
            '}';
    }
}


