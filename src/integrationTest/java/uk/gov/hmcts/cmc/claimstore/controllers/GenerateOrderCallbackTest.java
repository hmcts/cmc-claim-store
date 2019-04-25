package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.services.CallbackService;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class GenerateOrderCallbackTest extends MockSpringTest {

    @Test
    public void shouldPrepopulateFields() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackService.ABOUT_TO_START_CALLBACK)
            .andExpect(status().isOk())
            .andReturn();

        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        assertThat(responseData).hasSize(3);
        assertThat(LocalDate.parse(responseData.get("docUploadDeadline").toString()))
            .isAfterOrEqualTo(LocalDate.now().plusDays(33));
        assertThat(LocalDate.parse(responseData.get("eyewitnessUploadDeadline").toString()))
            .isAfterOrEqualTo(LocalDate.now().plusDays(33));
        assertThat(responseData).flatExtracting("directionList")
            .containsExactlyInAnyOrder("DOCUMENTS", "EYEWITNESS", "MEDIATION");
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        MvcResult mvcResult = makeRequest("not-a-real-callback")
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(mvcResult.getResolvedException())
            .isInstanceOfAny(BadRequestException.class);
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetailsTemp =  successfulCoreCaseDataStoreSubmitResponse();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.GENERATE_ORDER.getValue())
            .caseDetails(CaseDetails.builder()
                .id(caseDetailsTemp.getId())
                .data(caseDetailsTemp.getData())
                .build())
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .content(jsonMapper.toJson(callbackRequest))
            );

    }
}
