package uk.gov.hmcts.cmc.claimstore.controllers;

import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.controllers.CallbackController.ABOUT_TO_START_CALLBACK;
import static uk.gov.hmcts.cmc.claimstore.controllers.CallbackController.ABOUT_TO_SUBMIT_CALLBACK;
import static uk.gov.hmcts.cmc.claimstore.controllers.CallbackController.SUBMITTED_CALLBACK;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api"
    }
)
public class MoreTimeRequestedCallbackTest extends MockSpringTest {

    @SpyBean
    private MoreTimeRequestedNotificationService notificationService;

    @Test
    public void shouldReturnWithNoValidationErrorsOnAboutToStartIfAvailable() throws Exception {
        MvcResult mvcResult = makeRequest(ABOUT_TO_START_CALLBACK, LocalDate.now().plusDays(3), false)
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).isNull();
    }

    @Test
    public void shouldReturnWithValidationErrorsOnAboutToStartIfAlreadyRequested() throws Exception {
        MvcResult mvcResult = makeRequest(ABOUT_TO_START_CALLBACK, LocalDate.now().plusDays(3), true)
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(MoreTimeRequestRule.ALREADY_REQUESTED_MORE_TIME_ERROR);
    }

    @Test
    public void shouldReturnWithValidationErrorsOnAboutToStartIfAlreadyResponded() throws Exception {
        MvcResult mvcResult = makeRequest(
            ABOUT_TO_START_CALLBACK,
            LocalDate.now().plusDays(3),
            false,
            true
        )
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(MoreTimeRequestRule.ALREADY_RESPONDED_ERROR);
    }

    @Test
    public void shouldReturnWithValidationErrorsOnAboutToStartIfPastResponseDeadline() throws Exception {
        MvcResult mvcResult = makeRequest(ABOUT_TO_START_CALLBACK, LocalDate.now().minusDays(1), false)
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo(MoreTimeRequestRule.PAST_DEADLINE_ERROR);
    }

    @Test
    public void shouldModifyResponseDeadlineOnAboutToSubmit() throws Exception {
        LocalDate responseDeadline = LocalDate.now().plusDays(3);
        MvcResult mvcResult = makeRequest(ABOUT_TO_SUBMIT_CALLBACK, responseDeadline, false)
            .andExpect(status().isOk())
            .andReturn();

        AboutToStartOrSubmitCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            AboutToStartOrSubmitCallbackResponse.class
        );

        assertThat(LocalDate.parse(response.getData().get("responseDeadline").toString()))
            .isNotEqualTo(responseDeadline);

        assertThat(CCDYesNoOption.valueOf(response.getData().get("moreTimeRequested").toString()))
            .isEqualTo(CCDYesNoOption.YES);
    }

    @Test
    public void shouldSendNotificationOnSubmitted() throws Exception {
        LocalDate responseDeadline = LocalDate.now().plusDays(3);
        MvcResult mvcResult = makeRequest(SUBMITTED_CALLBACK, responseDeadline, false)
            .andExpect(status().isOk())
            .andReturn();

        SubmittedCallbackResponse response = deserializeObjectFrom(
            mvcResult,
            SubmittedCallbackResponse.class
        );

        verify(notificationService, once()).sendMail(
            eq(SampleClaim.SUBMITTER_EMAIL),
            any(),
            any(),
            eq("more-time-requested-notification-to-claimant-004MC931")
        );

        assertThat(response.getConfirmationBody()).isNull();
    }

    @Test
    public void shouldReturnErrorForUnknownCallback() throws Exception {
        makeRequest("not-a-real-callback", null, false)
            .andExpect(status().isBadRequest());
    }

    private ResultActions makeRequest(
        String callbackType,
        LocalDate responseDeadline,
        boolean moreTimeRequestedAlready
    ) throws Exception {
        return makeRequest(callbackType, responseDeadline, moreTimeRequestedAlready, false);
    }

    private ResultActions makeRequest(
        String callbackType,
        LocalDate responseDeadline,
        boolean moreTimeRequestedAlready,
        boolean respondedAlready
    ) throws Exception {
        CaseDetails caseDetailsTemp = successfulCoreCaseDataStoreSubmitResponse();
        caseDetailsTemp.getData().put("responseDeadline", responseDeadline);
        caseDetailsTemp.getData().put("moreTimeRequested",
            moreTimeRequestedAlready ? CCDYesNoOption.YES.name() : CCDYesNoOption.NO.name()
        );

        if (respondedAlready) {
            caseDetailsTemp.getData().put("respondedAt", LocalDateTimeFactory.nowInUTC());
        }

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", caseDetailsTemp.getId());
        caseDetails.put("case_data", caseDetailsTemp.getData());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
            .caseDetails(caseDetails)
            .build();

        return webClient
            .perform(post("/cases/callbacks/" + callbackType)
                .header(HttpHeaders.CONTENT_TYPE, "application/json")
                .content(jsonMapper.toJson(callbackRequest))
            );

    }
}
