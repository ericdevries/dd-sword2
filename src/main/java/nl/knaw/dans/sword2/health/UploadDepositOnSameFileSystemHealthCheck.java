package nl.knaw.dans.sword2.health;

import com.codahale.metrics.health.HealthCheck;
import nl.knaw.dans.sword2.config.CollectionConfig;
import nl.knaw.dans.sword2.core.service.FileService;

import java.util.List;

public class UploadDepositOnSameFileSystemHealthCheck extends HealthCheck {
    private final List<CollectionConfig> collectionConfigList;
    private final FileService fileService;

    public UploadDepositOnSameFileSystemHealthCheck(List<CollectionConfig> collectionConfigList, FileService fileService) {
        this.collectionConfigList = collectionConfigList;
        this.fileService = fileService;
    }

    @Override
    protected Result check() throws Exception {
        var result = Result.builder();
        var isValid = true;

        for (var collection : collectionConfigList) {
            var isSameFileStore = fileService.isSameFileSystem(collection.getUploads(), collection.getDeposits());

            if (!isSameFileStore) {
                result.withDetail(collection.getName(), "Upload and deposit path are on different file stores");
                isValid = false;
            }
        }

        if (isValid) {
            return result.healthy().build();
        }
        else {
            return result.unhealthy().withMessage("Some collections have their upload and deposit paths on different file stores").build();
        }
    }
}
