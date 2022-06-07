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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import nl.knaw.dans.sword2.core.DepositState;
import nl.knaw.dans.sword2.core.config.converter.StringByteSizeConverter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.List;

public class CollectionConfig {
    @NotEmpty
    private String name;
    @NotEmpty
    private String path;
    @NotNull
    private Path uploads;
    @NotNull
    private Path deposits;
    @Valid
    @NotNull
    @JsonDeserialize(converter = StringByteSizeConverter.class)
    private long diskSpaceMargin;

    public CollectionConfig() {

    }

    public CollectionConfig(String name, String path, Path uploads, Path deposits, long diskSpaceMargin, List<DepositState> autoClean) {
        this.name = name;
        this.path = path;
        this.uploads = uploads;
        this.deposits = deposits;
        this.diskSpaceMargin = diskSpaceMargin;
        this.autoClean = autoClean;
    }

    private List<DepositState> autoClean;

    public long getDiskSpaceMargin() {
        return diskSpaceMargin;
    }

    public void setDiskSpaceMargin(long diskSpaceMargin) {
        this.diskSpaceMargin = diskSpaceMargin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Path getUploads() {
        return uploads;
    }

    public void setUploads(Path uploads) {
        this.uploads = uploads;
    }

    public Path getDeposits() {
        return deposits;
    }

    public void setDeposits(Path deposits) {
        this.deposits = deposits;
    }

    public List<DepositState> getAutoClean() {
        return autoClean;
    }

    public void setAutoClean(List<DepositState> autoClean) {
        this.autoClean = autoClean;
    }

    @Override
    public String toString() {
        return "CollectionConfig{" +
            "name='" + name + '\'' +
            ", path='" + path + '\'' +
            ", uploads=" + uploads +
            ", deposits=" + deposits +
            ", autoClean=" + autoClean +
            '}';
    }
}
