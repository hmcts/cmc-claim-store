package uk.gov.hmcts.cmc.claimstore.deprecated.controllers;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.deprecated.MockSpringTest;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.MoreTimeRequestedNotificationService;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@TestPropertySource(
    properties = {
        "core_case_data.api.url=http://core-case-data-api",
        "doc_assembly.url=false"
    }
)
@Ignore
public class MoreTimeRequestedCallbackHandlerTest extends MockSpringTest {

    @SpyBean
    private MoreTimeRequestedNotificationService notificationService;

    @Test
    public void shouldReturnWithNoValidationErrorsOnAboutToStartIfAvailable() throws Exception {
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_START.getValue(), LocalDate.now().plusDays(3), false)
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
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_START.getValue(), LocalDate.now().plusDays(3), true)
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
            CallbackType.ABOUT_TO_START.getValue(),
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
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_START.getValue(), LocalDate.now().minusDays(1), false)
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
        LocalDate responseDeadline = LocalDate.now().plusDays(14);
        MvcResult mvcResult = makeRequest(CallbackType.ABOUT_TO_SUBMIT.getValue(), responseDeadline, false)
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
        MvcResult mvcResult = makeRequest(CallbackType.SUBMITTED.getValue(), responseDeadline, false)
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

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
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
