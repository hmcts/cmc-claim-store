package uk.gov.hmcts.cmc.claimstore.controllers.ioc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.claimstore.BaseMockSpringTest;
import uk.gov.hmcts.cmc.claimstore.models.idam.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.email.EmailService;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CASE;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "feature_toggles.create_claim_enabled=true"
    }
)
public class CreateClaimCallbackHandlerTest extends BaseMockSpringTest {

    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    public static final String REFERENCE_NO = "000LR001";

    @MockBean
    protected EmailService emailService;

    @BeforeEach
    public void setUp() {
        given(referenceNumberRepository.getReferenceNumberForLegal())
            .willReturn(REFERENCE_NO);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("caseworker-cmc-solicitor").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessful() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue())
            .andExpect(status().isOk())
            .andReturn();
        Map<String, Object> responseData = jsonMappingHelper.deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        ).getData();

        List<Map<String, Object>> respondents = (List<Map<String, Object>>) responseData.get("respondents");
        Map<String, Object> defendant = (Map<String, Object>) respondents.get(0).get("value");

        assertThat(defendant != null);
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CASE.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .header(HttpHeaders.AUTHORIZATION, AUTHORISATION_TOKEN)
                .content(jsonMappingHelper.toJson(callbackRequest))
            );
    }
}
