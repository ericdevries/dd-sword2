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

import io.dropwizard.auth.Auth;
import java.io.OutputStream;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.api.statement.Feed;
import org.glassfish.jersey.media.multipart.MultiPart;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/collection/{id}")
public interface CollectionResource {

    @GET
    @Produces(MediaType.APPLICATION_XML)
    Feed getCollection(@Context HttpHeaders headers, @Auth Depositor depositor);


    @POST
    @Consumes("multipart/*")
    @Produces(MediaType.APPLICATION_ATOM_XML)
    Response depositMultipart(MultiPart multiPart,
        @PathParam("id") String collectionId,
        @Context HttpHeaders headers,
        @Auth Depositor depositor
    );

    @POST
    @Consumes(MediaType.APPLICATION_ATOM_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML)
    Response depositAtom(@PathParam("id") String collectionId, @Context HttpHeaders headers, @Auth Depositor depositor);

    @POST
    @Consumes()
    @Produces(MediaType.APPLICATION_ATOM_XML)
    Response depositAnything(InputStream inputStream,
        @PathParam("id") String collectionId,
        @Context HttpHeaders headers,
        @Auth Depositor depositor
    );

}
