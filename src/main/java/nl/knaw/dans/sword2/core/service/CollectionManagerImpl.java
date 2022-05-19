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

import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.core.exceptions.CollectionNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionManagerImpl implements CollectionManager {
    private final List<CollectionConfig> collectionConfig;
    private final Map<String, CollectionConfig> collectionConfigByPath = new HashMap<>();
    private final Map<String, CollectionConfig> collectionConfigByName = new HashMap<>();

    public CollectionManagerImpl(List<CollectionConfig> collectionConfig) {
        this.collectionConfig = collectionConfig;

        for (var config : collectionConfig) {
            collectionConfigByPath.put(config.getPath(), config);
            collectionConfigByName.put(config.getName(), config);
        }
    }

    @Override
    public CollectionConfig getCollectionByPath(String id, Depositor depositor) throws CollectionNotFoundException {
        var config = collectionConfigByPath.get(id);

        if (config == null) {
            throw new CollectionNotFoundException(String.format("Collection with id %s could not be found", id));
        }

        // make it opaque as to why the user cannot access this collection by pretending
        // the collection does not exist if the user does not have access
        if (!depositor.getCollections().contains(config.getName())) {
            throw new CollectionNotFoundException(String.format("Collection with id %s could not be found", id));
        }

        return config;
    }

    @Override
    public CollectionConfig getCollectionByName(String id) throws CollectionNotFoundException {
        if (!collectionConfigByName.containsKey(id)) {
            throw new CollectionNotFoundException(String.format("Collection with id %s could not be found", id));
        }

        return collectionConfigByName.get(id);
    }

    @Override
    public List<CollectionConfig> getCollections(Depositor depositor) {
        return collectionConfig.stream().filter(collection -> depositor.getCollections().contains(collection.getName())).collect(Collectors.toList());
    }

    @Override
    public List<CollectionConfig> getCollections() {
        return collectionConfig;
    }
}
