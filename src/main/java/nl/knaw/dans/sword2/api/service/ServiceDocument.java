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
package nl.knaw.dans.sword2.api.service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceDocument {

    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String version;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private int maxUploadSize = -1;
    @XmlElement(name = "workspace")
    private List<ServiceWorkspace> workspaces;

    public List<ServiceWorkspace> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<ServiceWorkspace> workspaces) {
        this.workspaces = workspaces;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
}
