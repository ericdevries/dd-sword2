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
package nl.knaw.dans.sword2.api.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement(name = "generator", namespace = "http://www.w3.org/2005/Atom")
@XmlAccessorType(XmlAccessType.FIELD)
public class Generator {
    @XmlAttribute
    private URI uri;
    @XmlAttribute
    private String version;

    // never forget, always add an empty constructor
    public Generator() {

    }

    public Generator(URI uri, String version) {
        this.uri = uri;
        this.version = version;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Generator{" +
            "uri=" + uri +
            ", version='" + version + '\'' +
            '}';
    }
}
