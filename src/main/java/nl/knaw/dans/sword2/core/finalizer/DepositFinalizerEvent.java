
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
package nl.knaw.dans.sword2.core.finalizer;

public class DepositFinalizerEvent {

    private final String depositId;
    private final DepositFinalizerEventType eventType;

    public DepositFinalizerEvent(String depositId, DepositFinalizerEventType eventType) {
        this.depositId = depositId;
        this.eventType = eventType;
    }

    public DepositFinalizerEvent(String depositId) {
        this.depositId = depositId;
        this.eventType = DepositFinalizerEventType.FINALIZE;
    }

    public DepositFinalizerEventType getEventType() {
        return eventType;
    }

    public String getDepositId() {
        return depositId;
    }

    @Override
    public String toString() {
        return "DepositFinalizerEvent{" +
            "depositId='" + depositId + '\'' +
            ", eventType=" + eventType +
            '}';
    }
}
