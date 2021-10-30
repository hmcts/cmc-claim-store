package uk.gov.hmcts.cmc.claimstore.controllers.legaladvisor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDirectionOrder;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.legaladvisor.HearingCourt;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimDocument;
import uk.gov.hmcts.cmc.domain.models.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.domain.models.bulkprint.PrintRequestType.LEGAL_ORDER;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=http://doc-assembly-api"
    }
)
public class DrawOrderCallbackHandlerTest extends BaseMockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";
    private static final String DOCUMENT_BINARY_URL = "http://bla.binary.test";
    private static final String DOCUMENT_FILE_NAME = "sealed_claim.pdf";
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");

    @MockBean
    protected EmailService emailService;

    private static final CCDDocument DOCUMENT = CCDDocument
        .builder()
        .documentUrl(DOCUMENT_URL)
        .documentBinaryUrl(DOCUMENT_BINARY_URL)
        .documentFileName(DOCUMENT_FILE_NAME)
        .build();

    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
        CCDCollectionElement.<CCDClaimDocument>builder()
            .value(CCDClaimDocument.builder()
                .documentLink(DOCUMENT)
                .createdDatetime(DATE)
                .documentType(CCDClaimDocumentType.ORDER_DIRECTIONS)
                .build())
            .build();
    private UserDetails userDetails;

    @Before
    public void setUp() {
        given(documentManagementService
            .downloadDocument(
                eq(AUTHORISATION_TOKEN),
                any(ClaimDocument.class))).willReturn("template".getBytes());

        given(documentManagementService
            .getDocumentMetaData(
                eq(AUTHORISATION_TOKEN),
                any(String.class))).willReturn(ResourceLoader.successfulDocumentManagementDownloadResponse());

        userDetails = SampleUserDetails.builder().withRoles("caseworker-cmc-legaladvisor", "caseworker-cmc").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        given(directionOrderService.getHearingCourt(any())).willReturn(HearingCourt.builder().build());
        given(legalOrderService.print(eq(AUTHORISATION_TOKEN), any(Claim.class), any(CCDDocument.class)))
            .willReturn(List.of(
                BulkPrintDetails.builder().printRequestType(LEGAL_ORDER).printRequestId("coverId").build(),
                BulkPrintDetails.builder().printRequestType(LEGAL_ORDER).printRequestId("orderId").build())
            );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAddDraftDocumentToCaseDocumentsOnEventStart() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(23);
        List<Map<String, Object>> caseDocuments =
            (List<Map<String, Object>>) responseData.get("caseDocuments");
        Map<String, Object> document =
            (Map<String, Object>) caseDocuments.get(0).get("value");
        assertThat(document)
            .containsKey("createdDatetime")
            .containsKey("documentLink")
            .contains(
                entry("documentType", "ORDER_DIRECTIONS")
            );
    }

    @Test
    public void shouldNotifyPartiesAndBulkPrintLegalOrderAndSheetOnSubmittedEvent() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.SUBMITTED.getValue())
            .andExpect(status().isOk())
            .andReturn();
        SubmittedCallbackResponse response = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            SubmittedCallbackResponse.class
        );
        verify(orderDrawnNotificationService)
            .notifyClaimant(any(Claim.class));
        verify(orderDrawnNotificationService)
            .notifyDefendant(any(Claim.class));
        verify(legalOrderService).print(
            eq(AUTHORISATION_TOKEN),
            any(Claim.class),
            any(CCDDocument.class)
        );
        assertThat(response.getConfirmationHeader()).isNull();
        assertThat(response.getConfirmationBody()).isNull();
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        caseDetails.getData().put("draftOrderDoc", ImmutableMap.of("document_url", DOCUMENT_URL));
        caseDetails.getData().put("caseDocuments", ImmutableList.of(CLAIM_DOCUMENT));
        caseDetails.getData().put("directionOrder", CCDDirectionOrder.builder()
            .hearingCourtAddress(SampleData.getCCDAddress())
            .build());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.DRAW_ORDER.getValue())
            .caseDetails(caseDetails)
            .build();

        givenForStartOfEvent(caseDetails);

        givenForSubmitEvent(caseDetails);

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }

    private void givenForSubmitEvent(CaseDetails caseDetails) {
        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(caseDetails.getData().get("id").toString()),
            anyBoolean(),
            any()
        ))
            .thenReturn(caseDetails);
    }

    private void givenForStartOfEvent(CaseDetails caseDetails) {
        when(coreCaseDataApi.startEventForCaseWorker(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            anyString(),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(caseDetails.getData().get("id").toString()),
            anyString()
        ))
            .thenReturn(StartEventResponse.builder()
                .caseDetails(caseDetails)
                .eventId("eventId")
                .token("token")
                .build());
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        MvcResult mvcResult = makeRequest("not-a-real-callback")
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(CallbackException.class);
    }

    @Test
    public void shouldReturnErrorForUnsupportedRole() throws Exception {
        UserDetails userDetails = SampleUserDetails.builder()
            .withRoles("caseworker-cmc")
            .build();

        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);

        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isForbidden())
            .andReturn();

        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(ForbiddenActionException.class);
    }
}
