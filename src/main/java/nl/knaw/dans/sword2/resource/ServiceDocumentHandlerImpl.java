package nl.knaw.dans.sword2.resource;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import nl.knaw.dans.sword2.UriRegistry;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.config.UserConfig;
import nl.knaw.dans.sword2.models.service.ServiceCollection;
import nl.knaw.dans.sword2.models.service.ServiceDocument;
import nl.knaw.dans.sword2.models.service.ServiceWorkspace;

public class ServiceDocumentHandlerImpl implements
    ServiceDocumentHandler {

    private final List<UserConfig> userConfigs;
    private final List<CollectionConfig> collectionConfigs;
    private final URI baseUri;

    public ServiceDocumentHandlerImpl(List<UserConfig> userConfigs,
        List<CollectionConfig> collectionConfigs,
        URI baseUri
    ) {
        this.userConfigs = userConfigs;
        this.collectionConfigs = collectionConfigs;
        this.baseUri = baseUri;
    }

    @Override
    public Response getServiceDocument(HttpHeaders httpHeaders) {
        var service = new ServiceDocument();
        service.setVersion("2.0");
        //service.setMaxUploadSize(123095);

        var workspace = new ServiceWorkspace();
        workspace.setTitle("EASY SWORD2 Deposit Service");

        var collections = collectionConfigs.stream()
            .map(collection -> {
                var c = new ServiceCollection();
                c.setHref(baseUri.resolve("collection/" + collection.getPath()));
                c.setMediation(false);
                c.setTitle(collection.getName());
                c.setAcceptPackaging(UriRegistry.PACKAGE_BAGIT);

                return c;
            })
            .collect(Collectors.toList());

        workspace.setCollections(collections);

        service.setWorkspaces(List.of(workspace));

        return Response.status(Status.OK)
            .entity(service)
            .build();
    }
}
