package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.idam.models.GeneratePinResponse;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResendNewPinCallbackHandlerTest {
    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private UserService userService;

    @Mock
    private CaseMapper caseMapper;

    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private NotificationTemplates templates;

    @Mock
    private EmailTemplates emailTemplates;

    private ResendNewPinCallbackHandler resendNewPinCallbackHandler;
    private CallbackParams callbackParams;
    private static final String AUTHORISATION = "Bearer: aaaa";
    private static final String DEFENDANT_EMAIL_TEMPLATE = "Defendant Email PrintableTemplate";

    private CallbackRequest callbackRequest;

    private Claim sampleClaim = SampleClaim.getDefaultWithoutResponse();
    private Claim sampleLinkedClaim = SampleClaim.getClaimWithFullDefenceAlreadyPaid();

    @Before
    public void setUp() throws Exception {
        resendNewPinCallbackHandler = new ResendNewPinCallbackHandler(
            caseDetailsConverter,
            userService,
            caseMapper,
            claimIssuedNotificationService,
            notificationsProperties
        );

        callbackRequest = CallbackRequest
            .builder()
            .caseDetails(CaseDetails.builder().data(Collections.emptyMap()).build())
            .eventId(CaseEvent.RESEND_PIN.getValue())
            .build();

        callbackParams = CallbackParams.builder()
            .type(CallbackType.ABOUT_TO_SUBMIT)
            .request(callbackRequest)
            .params(ImmutableMap.of(CallbackParams.Params.BEARER_TOKEN, AUTHORISATION))
            .build();
    }

    @Test
    public void shouldReturnErrorWhenClaimIsAlreadyLinkedToDefendant() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(sampleLinkedClaim);

        AboutToStartOrSubmitCallbackResponse response
            = (AboutToStartOrSubmitCallbackResponse) resendNewPinCallbackHandler.handle(callbackParams);

        assertThat(response.getErrors())
            .contains("Claim has already been linked to defendant - cannot send notification");
    }

    @Test
    public void shouldReturnNewPinAndNewLetterHolderID() {
        when(caseDetailsConverter.extractClaim(any(CaseDetails.class))).thenReturn(sampleClaim);

        when(notificationsProperties.getTemplates()).thenReturn(templates);
        when(templates.getEmail()).thenReturn(emailTemplates);
        when(emailTemplates.getDefendantClaimIssued()).thenReturn(DEFENDANT_EMAIL_TEMPLATE);

        String letterHolderId = "333";
        GeneratePinResponse pinResponse = new GeneratePinResponse("PIN", letterHolderId);
        when(userService.generatePin(anyString(), eq(AUTHORISATION))).thenReturn(pinResponse);

        resendNewPinCallbackHandler.handle(callbackParams);

        verify(claimIssuedNotificationService).sendMail(
            eq(sampleClaim),
            eq(sampleClaim.getClaimData().getDefendant().getEmail().orElse(null)),
            eq("PIN"),
            eq(DEFENDANT_EMAIL_TEMPLATE),
            eq("defendant-issue-notification-" + sampleClaim.getReferenceNumber()),
            eq("Dr. John Smith")
        );
    }
}
