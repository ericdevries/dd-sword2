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
package nl.knaw.dans.sword2.core;

import java.nio.file.Path;
import java.time.OffsetDateTime;

public class Deposit {
    private String id;
    private String filename;
    private String mimeType;
    private String slug = null;
    private String md5 = null;
    private String packaging;
    private String depositor;
    private String bagName;
    private String swordToken;
    private String otherId;
    private String otherIdVersion;
    private OffsetDateTime created;
    private DepositState state;
    private String stateDescription;
    private Path path;
    private String collectionId;
    private boolean inProgress = false;
    private boolean metadataRelevant = true;
    private long contentLength = -1L;

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public String getOtherIdVersion() {
        return otherIdVersion;
    }

    public void setOtherIdVersion(String otherIdVersion) {
        this.otherIdVersion = otherIdVersion;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public String getSwordToken() {
        return swordToken;
    }

    public void setSwordToken(String swordToken) {
        this.swordToken = swordToken;
    }

    public String getBagName() {
        return bagName;
    }

    public void setBagName(String bagName) {
        this.bagName = bagName;
    }

    public DepositState getState() {
        return state;
    }

    public void setState(DepositState state) {
        this.state = state;
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public void setCreated(OffsetDateTime created) {
        this.created = created;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(String depositor) {
        this.depositor = depositor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCanonicalId() {
        if (this.slug == null) {
            return id;
        }

        return this.slug;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isMetadataRelevant() {
        return metadataRelevant;
    }

    public void setMetadataRelevant(boolean metadataRelevant) {
        this.metadataRelevant = metadataRelevant;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "Deposit{" +
            "id='" + id + '\'' +
            ", filename='" + filename + '\'' +
            ", mimeType='" + mimeType + '\'' +
            ", slug='" + slug + '\'' +
            ", md5='" + md5 + '\'' +
            ", packaging='" + packaging + '\'' +
            ", depositor='" + depositor + '\'' +
            ", bagName='" + bagName + '\'' +
            ", swordToken='" + swordToken + '\'' +
            ", created=" + created +
            ", state=" + state +
            ", stateDescription='" + stateDescription + '\'' +
            ", path=" + path +
            ", inProgress=" + inProgress +
            ", metadataRelevant=" + metadataRelevant +
            ", contentLength=" + contentLength +
            '}';
    }
}
