package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.defendant;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.claimstore.utils.VerificationModeUtils.once;

@RunWith(MockitoJUnitRunner.class)
public class PartAdmitCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private IntentionToProceedNotificationService intentionToProceedNotificationService;

    private PartAdmitCallbackHandler partAdmitCallbackHandler;

    @Before
    public void setup() {
        partAdmitCallbackHandler = new PartAdmitCallbackHandler(caseDetailsConverter,
            intentionToProceedNotificationService);
    }

    @Test
    public void shouldHandleDisputesAllEvent() {
        assert partAdmitCallbackHandler.handledEvents().contains(CaseEvent.PART_ADMISSION);
    }

    @Test
    public void shouldBeForCaseworkerRole() {
        assert partAdmitCallbackHandler.getSupportedRoles().contains(Role.CASEWORKER);
    }

    @Test
    public void shouldCallNotifyCaseworkersOnSubmittedCallback() {
        CallbackParams callbackParams = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .request(CallbackRequest.builder().build())
            .build();

        partAdmitCallbackHandler.callbacks()
            .get(callbackParams.getType())
            .execute(callbackParams);

        verify(intentionToProceedNotificationService, once()).notifyCaseworkers(any());
    }
}
