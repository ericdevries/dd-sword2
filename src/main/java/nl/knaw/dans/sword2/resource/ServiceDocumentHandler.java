package nl.knaw.dans.sword2.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@Path("/servicedocument")
public interface ServiceDocumentHandler {

    @GET
    Response getServiceDocument(@Context HttpHeaders httpHeaders);
}
