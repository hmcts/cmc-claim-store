package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.BaseIntegrationTest;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.fixtures.SampleUserDetails;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CREATE_CITIZEN_CLAIM;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getAmountBreakDown;
import static uk.gov.hmcts.cmc.ccd.sample.data.SampleData.getCCDCitizenCase;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_CITIZEN_PAYMENT;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class CreateCitizenClaimCallbackHandlerTest extends BaseIntegrationTest {
    private static final String AUTHORISATION_TOKEN = "Bearer let me in";
    private static final String REFERENCE_NO = "000MC001";

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;

    @Before
    public void setUp() {
        given(referenceNumberRepository.getReferenceNumberForCitizen())
            .willReturn(REFERENCE_NO);

        UserDetails userDetails = SampleUserDetails.builder().withRoles("citizen").build();
        given(userService.getUserDetails(AUTHORISATION_TOKEN)).willReturn(userDetails);
    }

    @Test
    public void shouldAddFieldsOnCaseWhenCallbackIsSuccessful() throws Exception {

        CCDCase data = getCCDCitizenCase(getAmountBreakDown());
        data.setPaymentNextUrl("http://nexturl.test");

        given(coreCaseDataApi.submitForCitizen(
            eq(AUTHORISATION_TOKEN),
            eq(SERVICE_TOKEN),
            eq(USER_ID),
            eq(JURISDICTION_ID),
            eq(CASE_TYPE_ID),
            eq(IGNORE_WARNING),
            any()
            )
        ).willReturn(CaseDetails.builder()
            .id(3L)
            .state(CREATE_CITIZEN_CLAIM.getValue())
            .data(caseDetailsConverter.convertToMap(data))
            .build());

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
            .contains(entry("responseDeadline", defendant.get("responseDeadline")))
            .contains(entry("channel", CITIZEN.name()))
            .contains(entry("previousServiceCaseReference", REFERENCE_NO))
            .containsKey("issuedOn");
    }

    private ResultActions makeRequest(String callbackType) throws Exception {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CREATE_CITIZEN_CLAIM.getValue())
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
