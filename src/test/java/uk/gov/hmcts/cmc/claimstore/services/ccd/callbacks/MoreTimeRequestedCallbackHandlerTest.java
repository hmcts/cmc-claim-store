package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;

@RunWith(MockitoJUnitRunner.class)
public class MoreTimeRequestedCallbackHandlerTest {

    @Mock
    private EventProducer eventProducer;
    @Mock
    private AppInsights appInsights;
    @Mock
    private ResponseDeadlineCalculator responseDeadlineCalculator;
    @Mock
    private MoreTimeRequestRule moreTimeRequestRule;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private Claim claim;

    private CallbackRequest callbackRequest;

    private MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler;

    @Before
    public void setUp() {
        moreTimeRequestedCallbackHandler = new MoreTimeRequestedCallbackHandler(
            eventProducer,
            appInsights,
            responseDeadlineCalculator,
            moreTimeRequestRule,
            caseDetailsConverter
        );
        claim = SampleClaim.getDefault();
        callbackRequest =
            CallbackRequest.builder()
                .eventId(CaseEvent.MORE_TIME_REQUESTED_PAPER.getValue())
                .caseDetails(CaseDetails
                    .builder()
                    .id(10L)
                    .data(Collections.emptyMap())
                    .build())
                .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(10L)
            .data(Collections.emptyMap())
            .build();
        when(caseDetailsConverter.extractClaim(caseDetails)).thenReturn(claim);

    }

    @Test
    public void shouldValidateRequestOnAboutToStartEvent() {
        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_START)
            .request(callbackRequest)
            .build();
        List<String> validationResults = ImmutableList.of("a", "b", "c");
        when(moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, newDeadline))
            .thenReturn(validationResults);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getErrors()).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldCompleteRequestOnAboutToSubmitEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();
        LocalDate deadline = LocalDate.parse("2019-12-11");
        when(responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn())).thenReturn(deadline);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getData()).containsExactly(
            entry("moreTimeRequested", CCDYesNoOption.YES),
            entry("responseDeadline", deadline)
        );
    }

    @Test
    public void shouldReturnErrorsOnAboutToSubmitEventIfRequestIsInvalid() {
        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .build();
        List<String> validationResults = ImmutableList.of("a", "b", "c");
        when(moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, newDeadline))
            .thenReturn(validationResults);
        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        assertThat(response.getErrors()).containsExactly("a", "b", "c");
    }

    @Test
    public void shouldGenerateEventsOnSubmitted() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .build();
        SubmittedCallbackResponse response = (SubmittedCallbackResponse)
            moreTimeRequestedCallbackHandler
                .handle(callbackParams);

        verify(eventProducer).createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().get()
        );
        verify(appInsights)
            .trackEvent(
                RESPONSE_MORE_TIME_REQUESTED_PAPER,
                REFERENCE_NUMBER,
                claim.getReferenceNumber());

        assertThat(response).isNotNull();
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.MID)
            .request(callbackRequest)
            .build();
        moreTimeRequestedCallbackHandler
            .handle(callbackParams);
    }
}
