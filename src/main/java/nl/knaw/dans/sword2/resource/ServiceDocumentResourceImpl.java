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
package nl.knaw.dans.sword2.resource;

import nl.knaw.dans.sword2.config.UriRegistry;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.api.service.ServiceCollection;
import nl.knaw.dans.sword2.api.service.ServiceDocument;
import nl.knaw.dans.sword2.api.service.ServiceWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceDocumentResourceImpl implements ServiceDocumentResource {
    private static final Logger log = LoggerFactory.getLogger(ServiceDocumentResourceImpl.class);

    private final List<CollectionConfig> collectionConfigs;
    private final URI baseUri;

    public ServiceDocumentResourceImpl(List<CollectionConfig> collectionConfigs, URI baseUri) {
        this.collectionConfigs = collectionConfigs;
        this.baseUri = baseUri;
    }

    @Override
    public Response getServiceDocument(HttpHeaders httpHeaders, Depositor depositor) {
        var service = new ServiceDocument();
        service.setVersion("2.0");

        var workspace = new ServiceWorkspace();
        workspace.setTitle("EASY SWORD2 Deposit Service");

        var collections = collectionConfigs.stream()
            .filter(collection -> depositor.getCollections().contains(collection.getName()))
            .map(collection -> {
                var c = new ServiceCollection();
                c.setHref(baseUri.resolve("collection/" + collection.getPath()));
                c.setMediation(false);
                c.setTitle(collection.getName());
                c.setAcceptPackaging(UriRegistry.PACKAGE_BAGIT);

                log.trace("Service collection for depositor {}: {}", depositor, c);

                return c;
            }).collect(Collectors.toList());

        workspace.setCollections(collections);

        service.setWorkspaces(List.of(workspace));

        var collectionIds = collectionConfigs.stream().map(CollectionConfig::getName).collect(Collectors.joining(", "));
        log.info("Returning service document for user {} and collections {}", depositor, collectionIds);

        return Response.status(Status.OK).entity(service).build();
    }
}
