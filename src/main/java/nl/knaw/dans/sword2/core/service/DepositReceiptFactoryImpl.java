
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

import nl.knaw.dans.sword2.core.Deposit;
import nl.knaw.dans.sword2.core.config.UriRegistry;
import nl.knaw.dans.sword2.api.entry.Entry;
import nl.knaw.dans.sword2.api.entry.Link;

import java.net.URI;

public class DepositReceiptFactoryImpl implements DepositReceiptFactory {

    private final URI baseUrl;

    public DepositReceiptFactoryImpl(URI baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Entry createDepositReceipt(Deposit deposit) {
        var id = deposit.getCanonicalId();
        var editURI = baseUrl.resolve("container/" + id);
        var statementURI = baseUrl.resolve("statement/" + id);
        var mediaURI = baseUrl.resolve("media/" + id);

        var entry = new Entry();
        entry.setId(editURI.toString());
        entry.setPackaging(UriRegistry.PACKAGE_BAGIT);
        entry.addLink(new Link(editURI, "edit", null));
        entry.addLink(new Link(editURI, UriRegistry.REL_SWORD_EDIT, null));
        entry.addLink(new Link(statementURI,
            UriRegistry.REL_STATEMENT,
            "application/atom+xml;type=feed"));
        entry.addLink(new Link(mediaURI, "edit-media", null));
        entry.setTreatment("[1] unpacking [2] verifying integrity [3] storing persistently");
        entry.setVerboseDescription(String.format("received successfully: %s; MD5: %s",
            deposit.getFilename(), deposit.getMd5()));

        return entry;
    }

    @Override
    public URI getDepositLocation(Deposit deposit) {
        return baseUrl.resolve("container/" + deposit.getCanonicalId());
    }
}
