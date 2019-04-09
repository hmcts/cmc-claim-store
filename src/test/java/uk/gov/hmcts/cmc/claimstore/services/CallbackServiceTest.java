package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;

import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_PAPER;

@RunWith(MockitoJUnitRunner.class)
public class CallbackServiceTest {

    private CallbackService callbackService;

    @Before
    public void setUp() {
        callbackService = new CallbackService();
    }

    @Test
    public void shouldGetCallbackForSupportedEvent() {
        CallbackService.Callback callback = callbackService
            .getCallbackFor(
                MORE_TIME_REQUESTED_PAPER.getValue(),
                CallbackService.ABOUT_TO_START_CALLBACK);
        assertNotNull(callback);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowIfUnsupportedEvent() {
        callbackService.getCallbackFor(
                "this event does not exist",
                CallbackService.ABOUT_TO_START_CALLBACK);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowIfUnimplementedCallbackForValidEvent() {
        callbackService.getCallbackFor(
                GENERATE_ORDER.getValue(),
                CallbackService.SUBMITTED_CALLBACK);
    }
}
