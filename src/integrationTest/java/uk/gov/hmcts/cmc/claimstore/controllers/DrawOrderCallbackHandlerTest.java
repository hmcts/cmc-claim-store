package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.document.DocumentManagementService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.legaladvisor.OrderDrawnNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.legaladvisor.LegalOrderService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=http://doc-assembly-api"
    }
)
public class DrawOrderCallbackHandlerTest extends MockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String DOCUMENT_URL = "http://bla.test";

    @MockBean
    private OrderDrawnNotificationService orderDrawnNotificationService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private LegalOrderService legalOrderService;
    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @Before
    public void setUp() throws URISyntaxException {
        given(documentManagementService
            .downloadDocument(
                AUTHORISATION_TOKEN,
                new URI(DOCUMENT_URL),
                null)).willReturn("template".getBytes());
        Claim claim = SampleClaim.builder().build();
        given(caseDetailsConverter.extractClaim(any(CaseDetails.class))).willReturn(claim);
    }

    @Test
    public void shouldAddDraftDocumentToEmptyCaseDocumentsOnEventStart() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(22);
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
        SubmittedCallbackResponse response = deserializeObjectFrom(
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
        CaseDetails caseDetailsTemp =  successfulCoreCaseDataStoreSubmitResponse();
        Map<String, Object> data = new HashMap<>(caseDetailsTemp.getData());
        data.put("draftOrderDoc",
            ImmutableMap.of("document_url", DOCUMENT_URL));

        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseDetailsTemp.getId())
            .data(data)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.DRAW_ORDER.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(callbackRequest))
            );
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        MvcResult mvcResult = makeRequest("not-a-real-callback")
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(CallbackException.class);
    }
}
