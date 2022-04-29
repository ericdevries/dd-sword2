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
package nl.knaw.dans.sword2.service;

import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.reader.BagReader;

import java.nio.file.Path;

public class BagItManagerImpl implements BagItManager {
    @Override
    public BagItMetaData getBagItMetaData(Path path, String depositId) throws Exception {
        try {
            var bag = new BagReader().read(path);
            var metadata = new BagItMetaData();

            for (var item: bag.getMetadata().getAll()) {
                System.out.println("KEY VALUE: " + item.getKey() + " - " + item.getValue());
            }
            var swordToken = bag.getMetadata().get("Is-Version-Of");

            if (swordToken != null) {
                for (var token : swordToken) {
                    if (token.startsWith("urn:uuid:")) {
                        metadata.setSwordToken("sword:" + token.substring("urn:uuid:".length()));
                    }
                    else {
                        System.out.println("INVALID TOKEN!");
                    }
                }
            }
            else {
                metadata.setSwordToken("sword:" + depositId);
            }

            metadata.setOtherId(getMetadata(bag, "Has-Organizational-Identifier", ""));
            metadata.setOtherId(getMetadata(bag, "Has-Organizational-Identifier-Version", ""));

            return metadata;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        throw new Exception("OH O");
    }

    String getMetadata(Bag bag, String key, String defaultValue) {
        var data = bag.getMetadata().get(key);

        if (data == null) {
            return defaultValue;
        }

        return data.stream().findFirst().orElse(defaultValue);
    }
}
