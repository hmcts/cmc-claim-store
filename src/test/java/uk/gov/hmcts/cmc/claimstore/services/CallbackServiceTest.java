package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.CallbackService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.GenerateOrderCallbackService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.MoreTimeRequestedCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_PAPER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SEALED_CLAIM_UPLOAD;

@RunWith(MockitoJUnitRunner.class)
public class CallbackServiceTest {

    @Mock
    private MoreTimeRequestedCallbackService moreTimeRequestedCallbackService;
    @Mock
    private GenerateOrderCallbackService generateOrderCallbackService;

    private CallbackService callbackDispatcher;

    @Before
    public void setUp() {
        callbackDispatcher = new CallbackService(
            moreTimeRequestedCallbackService,
            generateOrderCallbackService
        );
    }

    @Test
    public void shouldDispatchCallbackForMoreTimeRequested() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build())
            .eventId(MORE_TIME_REQUESTED_PAPER.getValue())
            .build();
        callbackDispatcher
            .dispatch("Bearer auth",
                CallbackType.ABOUT_TO_START,
                callbackRequest);

        verify(moreTimeRequestedCallbackService).execute(
            CallbackType.ABOUT_TO_START,
            callbackRequest
        );
    }

    @Test
    public void shouldDispatchCallbackForGenerateOrder() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder()
                .id(10L)
                .data(Collections.emptyMap())
                .build())
            .eventId(GENERATE_ORDER.getValue())
            .build();
        String bearerToken = "Bearer auth";
        callbackDispatcher
            .dispatch(bearerToken,
                CallbackType.ABOUT_TO_START,
                callbackRequest);

        verify(generateOrderCallbackService).execute(
            CallbackType.ABOUT_TO_START,
            callbackRequest,
            bearerToken
        );
    }

    @Test(expected = CallbackException.class)
    public void shouldThrowIfUnsupportedEventForCallback() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId(SEALED_CLAIM_UPLOAD.getValue())
            .build();
        callbackDispatcher
            .dispatch("Bearer auth",
                CallbackType.ABOUT_TO_START,
                callbackRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfUnknownEvent() {
        CallbackRequest callbackRequest = CallbackRequest
            .builder()
            .eventId("this event does not exist")
            .build();
        callbackDispatcher
            .dispatch("Bearer auth",
                CallbackType.ABOUT_TO_START,
                callbackRequest);
    }
}
