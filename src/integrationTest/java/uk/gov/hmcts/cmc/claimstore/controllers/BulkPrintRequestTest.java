package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.cmc.claimstore.MockedCoreCaseDataApiTest;
import uk.gov.hmcts.cmc.claimstore.services.staff.BulkPrintStaffNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.document.domain.Classification;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CLAIM_ISSUE_RECEIPT_UPLOAD;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulDocumentManagementUploadResponse;

@TestPropertySource(
    properties = {
        "send-letter.url=http://localhost:${wiremock.server.port}",
        "feature_toggles.async_event_operations_enabled=false"
    }
)
@AutoConfigureWireMock(port = 0)
public class BulkPrintRequestTest extends MockedCoreCaseDataApiTest {

    @MockBean
    private BulkPrintStaffNotificationService bulkPrintNotificationService;

    private final ClaimData submittedByClaimant = SampleClaimData.submittedByClaimant();

    @Before
    public void setUp() {
        final String externalId = submittedByClaimant.getExternalId().toString();
        final String caseId = citizenSampleCaseDetails.getId().toString();

        stubForSearchForCitizen(externalId);
        stubForStartForCitizen();
        stubForSubmitForCitizen(externalId);

        stubForStartEventForCitizen(caseId, CLAIM_ISSUE_RECEIPT_UPLOAD.getValue());
        stubForSubmitEventForCitizen(caseId,
            citizenStartEventResponse.getCaseDetails().getId().toString());
        stubForStartEventForCitizen(caseId, SEALED_CLAIM_UPLOAD.getValue());
        stubForStartEventForCitizen(caseId, ISSUE_CASE.getValue());

        given(authTokenGenerator.generate()).willReturn(AUTHORISATION_TOKEN);
        given(documentUploadClient
            .upload(anyString(), anyString(), anyString(), anyList(), any(Classification.class), anyList()))
            .willReturn(successfulDocumentManagementUploadResponse());
    }

    @Test
    public void shouldNotSendNotificationWhenEverythingIsOk() throws Exception {
        stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{ \"letter_id\":\"" + UUID.randomUUID().toString() + "\" }")
            )
        );

        MvcResult result = makeIssueClaimRequest(submittedByClaimant, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService, never())
            .notifyFailedBulkPrint(
                anyList(),
                eq(deserializeObjectFrom(result, Claim.class)));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpClientError() throws Exception {

        stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            )
        );

        makeIssueClaimRequest(submittedByClaimant, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }

    @Test
    public void shouldSendNotificationWhenBulkPrintFailsWithHttpServerError() throws Exception {

        stubFor(post(urlEqualTo("/letters"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .withBody("Internal server error occurred")));

        makeIssueClaimRequest(submittedByClaimant, AUTHORISATION_TOKEN)
            .andExpect(status().isOk())
            .andReturn();

        verify(bulkPrintNotificationService)
            .notifyFailedBulkPrint(
                anyList(),
                any(Claim.class));
    }
}
