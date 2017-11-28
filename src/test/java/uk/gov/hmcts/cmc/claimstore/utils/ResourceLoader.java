package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.document.domain.UploadResponse;

public class ResourceLoader {
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    private ResourceLoader() {
    }

    public static UploadResponse successfulDocumentManagementUploadResponse() {
        final String response = new ResourceReader().read("/document-management/response.success.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse() {
        final String response = new ResourceReader().read("/document-management/response.failure.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }
}
