package uk.gov.hmcts.cmc.claimstore.utils;

import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.UploadResponse;

import java.util.List;
import java.util.Map;

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
        String response = new ResourceReader().read("/core-case-data/start-response.success.json");
        return jsonMapper.fromJson(response, StartEventResponse.class);
    }

    public static CaseDetails successfulCoreCaseDataStoreSubmitResponse() {
        String response = new ResourceReader().read("/core-case-data/submit-response.success.json");
        return jsonMapper.fromJson(response, CaseDetails.class);
    }

    public static CaseDetails caseWithReferenceNumber(String referenceNumber) {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();

        Map<String, Object> data = caseDetails.getData();
        data.put("referenceNumber", referenceNumber);
        return caseDetails;
    }

    public static List<CaseDetails> listOfCaseDetails() {
        String response = new ResourceReader().read("/core-case-data/search-response.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }
}
