package uk.gov.hmcts.cmc.claimstore;

import org.junit.Test;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCaseDetails;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleStartEventResponse;
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
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://localhost:${wiremock.server.port}"
    }
)
@AutoConfigureWireMock(port = 0)
public class MockedCoreCaseDataApiTest extends BaseSaveTest {

    private final CaseDetails legalRepresentativeSampleCaseDetails =
        SampleCaseDetails.builder().buildLegalCaseDetails();
    private final CaseDetails citizenSampleCaseDetails =
        SampleCaseDetails.builder().buildCitizenCaseDetails();
    private final StartEventResponse legalCaseStartEventResponse =
        SampleStartEventResponse.builder().buildLegalCaseStartEventResponse();
    private final StartEventResponse citizenStartEventResponse =
        SampleStartEventResponse.builder().buildCitizenStartEventResponse();

    @Test
    public void shouldSuccessfullySubmitClaimForRepresentative() throws Exception {

        final ClaimData legalRepresentativeClaimData = SampleClaimData.submittedByLegalRepresentative();
        final String externalId = legalRepresentativeClaimData.getExternalId().toString();

        stubForSearchForCaseWorkers(externalId);
        stubForStartForCaseworker();
        stubForSubmitForCaseworker(externalId);
        stubForStartEventForCaseWorker(legalRepresentativeSampleCaseDetails.getId().toString(),
                                       SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForCaseWorker(legalRepresentativeSampleCaseDetails.getId().toString(),
                                       legalCaseStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForCaseWorker(legalRepresentativeSampleCaseDetails.getId().toString(), ISSUE_CASE.getValue());


        given(authTokenGenerator.generate()).willReturn(SOLICITOR_AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        makeIssueClaimRequest(legalRepresentativeClaimData, SOLICITOR_AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldSuccessfullySubmitClaimForCitizen() throws Exception {
        final ClaimData submittedByClaimant = SampleClaimData.submittedByClaimant();
        final String externalId = submittedByClaimant.getExternalId().toString();

        stubForSearchForCitizen(externalId);
        stubForStartForCitizen();
        stubForSubmitForCitizen(externalId);
        stubForStartEventForCitizen(citizenSampleCaseDetails.getId().toString(),
                                    SEALED_CLAIM_UPLOAD.getValue());
        stubForSubmitEventForCitizen(citizenSampleCaseDetails.getId().toString(),
                                     citizenStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForCitizen(citizenSampleCaseDetails.getId().toString(),
                                    ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());

        makeIssueClaimRequest(submittedByClaimant, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();
    }

    public void stubForSearchForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID + "/cases"
            + "?" + "case.externalId=" + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(okForJson(Collections.emptyList()))
        );
    }

    public void stubForSearchForCaseWorkers(String externalId) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID + "/cases"
            + "?" + "case.externalId=" + externalId + "&" + "sortDirection=desc" + "&" + "page=1";

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
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(legalCaseStartEventResponse)))
        );
    }

    public void stubForStartForCitizen() {
        final String URI = "/citizens/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
            +"/event-triggers/" + CREATE_CASE.getValue() + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenStartEventResponse)))
        );
    }

    public void stubForSubmitForCaseworker(String externalId) {
        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID
            +"/case-types/"+ CASE_TYPE_ID +"/cases" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(legalRepresentativeSampleCaseDetails)))
        );
    }

    public void stubForSubmitForCitizen(String externalId) {
        final String URI = "/citizens/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID
            +"/case-types/"+ CASE_TYPE_ID +"/cases" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(externalId))
                .willReturn(aResponse()
                    .withStatus(HTTP_OK)
                    .withBody(jsonMapper.toJson(citizenSampleCaseDetails)))
        );
    }

    public void stubForStartEventForCaseWorker(String caseId, String eventTriggerType) {

        final String URI = "/caseworkers/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
            +"/cases/"+ caseId +"/event-triggers/"+ eventTriggerType + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(SOLICITOR_AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(legalCaseStartEventResponse)))
        );
    }

    public void stubForStartEventForCitizen(String caseId, String eventTriggerType) {

        final String URI = "/citizens/" + USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"+ CASE_TYPE_ID
            +"/cases/"+ caseId +"/event-triggers/"+ eventTriggerType + "/token";

        stubFor(get(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenStartEventResponse)))
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
                .withBody(jsonMapper.toJson(legalRepresentativeSampleCaseDetails)))
        );
    }

    public void stubForSubmitEventForCitizen(String caseId, String eventId) {
        final String URI = "/citizens/"+ USER_ID +"/jurisdictions/"+ JURISDICTION_ID +"/case-types/"
            + CASE_TYPE_ID +"/cases/"+ caseId +"/events" + "?" + "ignore-warning=" + IGNORE_WARNING;

        stubFor(post(urlEqualTo(URI))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORISATION_TOKEN))
            .withRequestBody(containing(eventId))
            .willReturn(aResponse()
                .withStatus(HTTP_OK)
                .withBody(jsonMapper.toJson(citizenSampleCaseDetails)))
        );
    }
}
