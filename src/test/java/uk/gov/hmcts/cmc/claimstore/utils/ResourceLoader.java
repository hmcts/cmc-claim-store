package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

public class ResourceLoader {
    private static final JsonMapper jsonMapper = JsonMapperFactory.create();

    private ResourceLoader() {
    }

    public static UploadResponse successfulDocumentManagementUploadResponse() {
        String response = new ResourceReader().read("/document-management/response.success.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }

    public static UploadResponse unsuccessfulDocumentManagementUploadResponse() {
        String response = new ResourceReader().read("/document-management/response.failure.json");
        return jsonMapper.fromJson(response, UploadResponse.class);
    }

    public static StartEventResponse successfulCoreCaseDataStoreStartResponse() {
        final String response = new ResourceReader().read("/core-case-data/start-response.success.json");
        return jsonMapper.fromJson(response, StartEventResponse.class);
    }

    public static CaseDetails successfulCoreCaseDataStoreSubmitResponse() {
        final String response = new ResourceReader().read("/core-case-data/submit-response.success.json");
        return jsonMapper.fromJson(response, CaseDetails.class);
    }
}
