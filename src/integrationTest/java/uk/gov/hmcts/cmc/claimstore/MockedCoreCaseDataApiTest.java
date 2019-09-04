package uk.gov.hmcts.cmc.claimstore;

import org.junit.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.document.domain.Classification;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.submitEventResponseSuccessAsFile;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreStartResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.startEventResponseSuccessAsFile;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitRepresentativeResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
public class MockedCoreCaseDataApiTest extends BaseSaveTest {

    final CaseDetails submittedCaseDetailsForRepresentative = successfulCoreCaseDataStoreSubmitRepresentativeResponse();
    final StartEventResponse submittedStartEventResponseForRepresentative = successfulCoreCaseDataStoreStartResponse();

    @Test
    public void shouldSubmitClaimForCaseWorkerForRepresentative() throws Exception {

        final ClaimData submittedByLegalRepresentative = SampleClaimData.submittedByLegalRepresentative();
        final String externalID = submittedByLegalRepresentative.getExternalId().toString();

        stubForSearchForCaseWorkers(externalID);
        stubForStartForCaseworker();
        stubForSubmitForCaseworker(externalID);
        stubForStartEventForCaseWorker(submittedCaseDetailsForRepresentative.getId().toString(),
                                       SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForCaseWorker(submittedCaseDetailsForRepresentative.getId().toString(),
                                        submittedStartEventResponseForRepresentative.getCaseDetails().getId().toString());
        stubForStartEventForCaseWorker(submittedCaseDetailsForRepresentative.getId().toString(), ISSUE_CASE.getValue());


        given(authTokenGenerator.generate()).willReturn(SOLICITOR_AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        makeIssueClaimRequest(submittedByLegalRepresentative, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    public void stubForSearchForCaseWorkers(String externalID) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID + "/cases"
            + "?" + "case.externalId=" + externalID + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    public void stubForStartForCaseworker() {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/event-triggers/" + CREATE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(okForJson(successfulCoreCaseDataStoreStartResponse()))
        );
    }

    public void stubForSubmitForCaseworker(String externalID) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID
                           +"/case-types/"+ CASE_TYPE_ID +"/cases" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalID))
            .willReturn(okForJson(submittedCaseDetailsForRepresentative))
        );
    }

    public void stubForStartEventForCaseWorker(String caseId, String eventTriggerType) {

        //Logger.getGlobal().info(startEventResponseSuccessAsFile());
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
                           +"/cases/"+ caseId +"/event-triggers/"+ eventTriggerType + "/token";

            stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(startEventResponseSuccessAsFile()))
        );
    }

   public void stubForSubmitEventForCaseWorker(String caseId, String eventId) {
        final String URI = "/caseworkers/"+ USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"
                           + CASE_TYPE_ID +"/cases/"+ caseId +"/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                        .withStatus(HTTP_OK)
                        .withBody(submitEventResponseSuccessAsFile()))
        );
    }
}
