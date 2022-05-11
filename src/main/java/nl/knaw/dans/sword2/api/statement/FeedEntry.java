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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "entry")
@XmlAccessorType(XmlAccessType.FIELD)
public class FeedEntry {

    @XmlElement
    private String id;
    @XmlElement(name = "title")
    private TextElement title;
    @XmlElement(name = "summary")
    private TextElement summary;
    @XmlElement
    private FeedContent content;

    public FeedEntry() {

    }

    public FeedEntry(String id, TextElement title, TextElement summary, FeedContent content) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TextElement getTitle() {
        return title;
    }

    public void setTitle(TextElement title) {
        this.title = title;
    }

    public TextElement getSummary() {
        return summary;
    }

    public void setSummary(TextElement summary) {
        this.summary = summary;
    }

    public FeedContent getContent() {
        return content;
    }

    public void setContent(FeedContent content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "FeedEntry{" + "id='" + id + '\'' + ", title=" + title + ", summary=" + summary + ", content=" + content + '}';
    }
}
