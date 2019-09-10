package uk.gov.hmcts.cmc.claimstore.utils;

import com.google.common.collect.ImmutableList;
import uk.gov.hmcts.cmc.claimstore.processors.JsonMapper;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.utils.ResourceReader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Document;
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

    public static CaseDetails successfulCoreCaseDataStoreSubmitRepresentativeResponse() {
        String response = new ResourceReader().read("/core-case-data/submit-representative-response.success.json");
        return jsonMapper.fromJson(response, CaseDetails.class);
    }

    public static StartEventResponse successfulCoreCaseDataStoreStartResponseWithLinkedDefendant() {
        String response = new ResourceReader().read("/core-case-data/start-response-defendant.success.json");
        return jsonMapper.fromJson(response, StartEventResponse.class);
    }

    public static CaseDetails successfulCoreCaseDataStoreSubmitResponse() {
        String response = getResource("/core-case-data/submit-response.success.json");
        return jsonMapper.fromJson(response, CaseDetails.class);
    }

    public static CaseDetails successfulCoreCaseDataStoreSubmitResponseWithDQ() {
        String response = getResource("/core-case-data/submit-response.success-with-dq.json");
        return jsonMapper.fromJson(response, CaseDetails.class);
    }

    private static String getResource(String resourceName) {
        return new ResourceReader().read(resourceName);
    }

    public static CaseDetails successfulCoreCaseDataStoreSubmitResponseWithMoreTimeExtension() {
        String response = getResource("/core-case-data/submit-response.success.json")
            .replace("2020-02-06", "2020-02-19");
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

    public static List<CaseDetails> listOfCaseDetailsWithMoreTimeExtension() {
        String response = new ResourceReader().read("/core-case-data/search-response.success.json")
            .replace("2020-02-06", "2020-02-18");

        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static List<CaseDetails> listOfCaseDetailsWithLinkedDefendant() {
        String response = new ResourceReader().read("/core-case-data/search-response-linked-defendant.success.json")
            .replace("2020-02-06", "2020-02-18");

        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static List<CaseDetails> listOfCaseDetailsWithDefendant() {
        String response = new ResourceReader().read("/core-case-data/search-response-with-def.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static List<CaseDetails> listOfCaseDetailsWithCCJ() {
        String response = new ResourceReader().read("/core-case-data/search-response-CCJ.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static List<CaseDetails> listOfCaseDetailsWithOfferCounterSigned() {
        String response
            = new ResourceReader().read("/core-case-data/search-response-with-offer-counter-signed.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static List<CaseDetails> listOfCaseDetailsWithDefResponse() {
        String response = new ResourceReader().read("/core-case-data/search-response-with-def-res.success.json");
        return ImmutableList.of(jsonMapper.fromJson(response, CaseDetails.class));
    }

    public static Document successfulDocumentManagementDownloadResponse() {
        String response = new ResourceReader().read("/document-management/download.success.json");
        return jsonMapper.fromJson(response, Document.class);
    }
}
