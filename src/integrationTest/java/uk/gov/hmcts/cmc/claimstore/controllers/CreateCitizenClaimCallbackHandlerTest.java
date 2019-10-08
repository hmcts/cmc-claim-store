package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class CreateCitizenClaimCallbackHandlerTest extends MockSpringTest {
    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String REFERENCE_NO = "000MC001";

    @Before
    public void setUp() {
        given(referenceNumberRepository.getReferenceNumberForCitizen())
            .willReturn(REFERENCE_NO);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessful() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) responseData.get("respondents");
        Map<String, Object> defendant = (Map<String, Object>) respondents.get(0).get("value");

        assertThat(defendant)
            .containsEntry("responseDeadline", defendant.get("responseDeadline"));
        assertThat(responseData)
            .contains(entry("channel", CITIZEN.name()))
            .contains(entry("previousServiceCaseReference", REFERENCE_NO))
            .containsKey("issuedOn");
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE,"application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMapper.toJson(callbackRequest))
            );
    }
}
