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
package nl.knaw.dans.sword2.api.statement;

import nl.knaw.dans.sword2.api.entry.Link;
import nl.knaw.dans.sword2.api.error.Generator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "feed")
@XmlAccessorType(XmlAccessType.FIELD)
public class Feed {

    @XmlElement
    private String id;
    @XmlElement(namespace = "http://www.w3.org/2005/Atom")
    private Generator generator;
    @XmlElement(name = "link")
    private List<Link> links = new ArrayList<>();
    @XmlElement
    private TextElement title;
    @XmlElement(name = "author")
    private List<FeedAuthor> authors = new ArrayList<>();
    @XmlElement
    private String updated;
    @XmlElement(name = "entry")
    private List<FeedEntry> entries = new ArrayList<>();
    @XmlElement
    private FeedCategory category;

    public Generator getGenerator() {
        return generator;
    }

    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public void addLink(Link link) {
        this.links.add(link);
    }

    public List<FeedAuthor> getAuthors() {
        return authors;
    }

    public void setAuthors(List<FeedAuthor> authors) {
        this.authors = authors;
    }

    public TextElement getTitle() {
        return title;
    }

    public void setTitle(TextElement title) {
        this.title = title;
    }
    //
    //    public OffsetDateTime getUpdated() {
    //        return updated;
    //    }
    //
    //    public void setUpdated(OffsetDateTime updated) {
    //        this.updated = updated;
    //    }

    public List<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<FeedEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(FeedEntry entry) {
        this.entries.add(entry);
    }

    public FeedCategory getCategory() {
        return category;
    }

    public void setCategory(FeedCategory category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "Feed{" +
            "id='" + id + '\'' +
            ", links=" + links +
            ", title=" + title +
            ", authors=" + authors +
            ", entries=" + entries +
            ", category=" + category +
            '}';
    }
}
