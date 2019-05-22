package uk.gov.hmcts.cmc.claimstore.controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
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

    @Test
    public void shouldAddDraftDocumentToEmptyCaseDocumentsOnEventStart() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_START.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(1);
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
