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

import java.net.URI;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceCollection {

    @XmlAttribute
    private URI href;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    private String title;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String acceptPackaging;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private boolean mediation;

    public URI getHref() {
        return href;
    }

    public void setHref(URI href) {
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isMediation() {
        return mediation;
    }

    public void setMediation(boolean mediation) {
        this.mediation = mediation;
    }

    public String getAcceptPackaging() {
        return acceptPackaging;
    }

    public void setAcceptPackaging(String acceptPackaging) {
        this.acceptPackaging = acceptPackaging;
    }

    @Override
    public String toString() {
        return "ServiceCollection{" +
            "href=" + href +
            ", title='" + title + '\'' +
            ", acceptPackaging='" + acceptPackaging + '\'' +
            ", mediation=" + mediation +
            '}';
    }
}
