package uk.gov.hmcts.cmc.claimstore;

import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.document.domain.UploadResponse;

public class DataHelper {
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    private DataHelper() {
    }

    public static UploadResponse documentManagementUploadResponse() {
        final String response = new ResourceReader().read("/document_management_response.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }
}
