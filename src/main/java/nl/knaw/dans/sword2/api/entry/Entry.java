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

package nl.knaw.dans.sword2.api.entry;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.FIELD)
public class Entry {

    @XmlElement
    private String title;
    @XmlElement
    private String id;
    @XmlElement(name = "link")
    private List<Link> links = new ArrayList<>();
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String packaging;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String treatment;
    @XmlElement(namespace = "http://purl.org/net/sword/terms/")
    private String verboseDescription;

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getVerboseDescription() {
        return verboseDescription;
    }

    public void setVerboseDescription(String verboseDescription) {
        this.verboseDescription = verboseDescription;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addLink(Link link) {
        this.links.add(link);
    }

    @Override
    public String toString() {
        return "Entry{" +
            "title='" + title + '\'' +
            ", id='" + id + '\'' +
            ", links=" + links +
            ", packaging='" + packaging + '\'' +
            ", treatment='" + treatment + '\'' +
            ", verboseDescription='" + verboseDescription + '\'' +
            '}';
    }
}
