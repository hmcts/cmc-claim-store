package uk.gov.hmcts.cmc.claimstore.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.document.domain.UploadResponse;

public class ResourceLoader {
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    private ResourceLoader() {
    }

    public static UploadResponse documentManagementUploadResponse() {
        final String response = new ResourceReader().read("/document-management-response.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }


    public static UploadResponse failedDocumentManagementUploadResponse() {
        final String response = "{\n"
            + "  \"_embedded\": {\n"
            + "    \"documents\": []"
            + "}"
            + "}";

        return jsonMapper.fromJson(response, UploadResponse.class);
    }
}
