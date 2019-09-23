package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ISSUE_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.LA_COMPLEX_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REVIEW_COMPLEX_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CLAIM_ISSUED_CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.COMPLICATED_FOR_ORDER_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.NOT_TOO_COMPLICATED_FOR_LA;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RETURNED_TO_LA_FROM_JUDGE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.TRANSFERRED_OUT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class AppInsightsCallbackHandlerTest {
    private AppInsightsCallbackHandler appInsightsCallbackHandler;
    @Mock
    private AppInsights appInsights;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    private CallbackRequest callbackRequest;
    private CallbackParams callbackParams;

    @Before
    public void setUp() {
        appInsightsCallbackHandler = new AppInsightsCallbackHandler(appInsights, caseDetailsConverter);
    }

    @Test
    public void shouldRaiseAppInsightForActionReviewCommentsEvent() {
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(ACTION_REVIEW_COMMENTS.name())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        appInsightsCallbackHandler.handle(callbackParams);

        verify(appInsights).trackEvent(RETURNED_TO_LA_FROM_JUDGE, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void shouldRaiseAppInsightForComplexCaseEvent() {
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(LA_COMPLEX_CASE.name())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        appInsightsCallbackHandler.handle(callbackParams);

        verify(appInsights).trackEvent(COMPLICATED_FOR_ORDER_PILOT, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void shouldRaiseAppInsightForReviewComplexCaseEvent() {
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(REVIEW_COMPLEX_CASE.name())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        appInsightsCallbackHandler.handle(callbackParams);

        verify(appInsights).trackEvent(NOT_TOO_COMPLICATED_FOR_LA, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void shouldRaiseAppInsightForWaitingTransferEvent() {
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(WAITING_TRANSFER.name())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        appInsightsCallbackHandler.handle(callbackParams);

        verify(appInsights).trackEvent(TRANSFERRED_OUT, REFERENCE_NUMBER, claim.getReferenceNumber());
    }

    @Test
    public void shouldNotRaiseAppInsightWhenEventIsNotSupported() {
        Claim claim = SampleClaim.builder().build();
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(claim);

        callbackRequest = CallbackRequest
            .builder()
            .eventId(ISSUE_CASE.name())
            .caseDetails(CaseDetails.builder().build())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(callbackRequest)
            .params(ImmutableMap.of(BEARER_TOKEN, BEARER_TOKEN))
            .build();

        appInsightsCallbackHandler.handle(callbackParams);

        verify(appInsights, never()).trackEvent(CLAIM_ISSUED_CITIZEN, REFERENCE_NUMBER, claim.getReferenceNumber());
    }
}
