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
package nl.knaw.dans.sword2.core.service;

import nl.knaw.dans.sword2.core.DepositState;

public class DepositProperties {

    private String bagStoreBagId;
    private String dataverseBagId;
    private String creationTimestamp;
    private String depositOrigin;
    private String depositorUserId;
    private DepositState state;
    private String stateDescription;
    private String bagStoreBagName;
    private String dataverseSwordToken;
    private String md5;
    private String contentType;


    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDepositorUserId() {
        return depositorUserId;
    }

    public void setDepositorUserId(String depositorUserId) {
        this.depositorUserId = depositorUserId;
    }

    public String getBagStoreBagId() {
        return bagStoreBagId;
    }

    public void setBagStoreBagId(String bagStoreBagId) {
        this.bagStoreBagId = bagStoreBagId;
    }

    public String getDataverseBagId() {
        return dataverseBagId;
    }

    public void setDataverseBagId(String dataverseBagId) {
        this.dataverseBagId = dataverseBagId;
    }

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(String creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public String getDepositOrigin() {
        return depositOrigin;
    }

    public void setDepositOrigin(String depositOrigin) {
        this.depositOrigin = depositOrigin;
    }

    public DepositState getState() {
        return state;
    }

    public void setState(DepositState state) {
        this.state = state;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(String stateDescription) {
        this.stateDescription = stateDescription;
    }

    public String getBagStoreBagName() {
        return bagStoreBagName;
    }

    public void setBagStoreBagName(String bagStoreBagName) {
        this.bagStoreBagName = bagStoreBagName;
    }

    public String getDataverseSwordToken() {
        return dataverseSwordToken;
    }

    public void setDataverseSwordToken(String dataverseSwordToken) {
        this.dataverseSwordToken = dataverseSwordToken;
    }

    @Override
    public String toString() {
        return "DepositProperties{" + "bagStoreBagId='" + bagStoreBagId + '\''
            + ", dataverseBagId='" + dataverseBagId + '\''
            + ", creationTimestamp='" + creationTimestamp + '\'' + ", depositOrigin='"
            + depositOrigin + '\'' + ", depositorUserId='"
            + depositorUserId + '\'' + ", state=" + state + ", stateDescription='"
            + stateDescription + '\'' + ", bagStoreBagName='"
            + bagStoreBagName + '\'' + ", dataverseSwordToken='" + dataverseSwordToken + '\'' + '}';
    }
}
