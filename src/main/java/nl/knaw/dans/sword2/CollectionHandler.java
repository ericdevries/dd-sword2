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
package nl.knaw.dans.sword2;

import nl.knaw.dans.sword2.models.Entry;
import nl.knaw.dans.sword2.openapi.api.CollectionDto;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;

@Path("/collection/{id}")
public interface CollectionHandler {
    //    @POST
    //    @Consumes({"application/json"})
    //    @Produces({"application/json"})
    CollectionDto deposit(CollectionDto input);

    @GET
    @Produces(MediaType.APPLICATION_XML)
    Entry getDeposit(@Context HttpHeaders headers);

    @POST
    @Consumes()
    @Produces(MediaType.APPLICATION_XML)
    Response depositAnything(InputStream inputStream, @Context HttpHeaders headers);

    //    @POST
    //    @Consumes({"application/atom+xml"})
    //    @Produces({"application/json"})
    CollectionDto depositAtom(InputStream inputStream);
}
