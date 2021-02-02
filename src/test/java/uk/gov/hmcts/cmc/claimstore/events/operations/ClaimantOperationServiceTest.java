package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.HwfClaimNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimForHwF;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleHwfClaim;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_INFO_REQUIRED_FOR_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.HWF_APPLICATION_PENDING;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantOperationServiceTest {

    public static final String CLAIMANT_EMAIL_TEMPLATE = "Claimant Email Template";
    public static final String REPRESENTATIVE_EMAIL_TEMPLATE = "Representative Email Template";
    public static final Claim CLAIM_HWF_PENDING = SampleHwfClaim.getDefaultHwfPending();
    public static final Claim CLAIM_HWF_AWAITING_RESPONSE = SampleHwfClaim.getDefaultAwaitingResponseHwf();
    public static final String AUTHORISATION = "AUTHORISATION";
    public static final String SUBMITTER_NAME = "submitter-name";
    public static final String REPRESENTATIVE_EMAIL = "representative@Email.com";

    private ClaimantOperationService claimantOperationService;
    @Mock
    private ClaimIssuedNotificationService claimIssuedNotificationService;
    @Mock
    private NotificationsProperties notificationProperties;
    @Mock
    private NotificationTemplates templates;
    @Mock
    private EmailTemplates emailTemplates;
    @Mock
    private ClaimCreationEventsStatusService eventsStatusService;
    @Mock
    private HwfClaimNotificationService hwfClaimNotificationService;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private AppInsights appInsights;

    @Before
    public void before() {
        claimantOperationService = new ClaimantOperationService(claimIssuedNotificationService,
            notificationProperties, eventsStatusService, hwfClaimNotificationService);

        given(notificationProperties.getTemplates()).willReturn(templates);
        given(templates.getEmail()).willReturn(emailTemplates);
    }

    @Test
    public void shouldNotifyCitizenForAwaitingResponse() {
        //given
        given(emailTemplates.getClaimantHwfUpdate()).willReturn(CLAIMANT_EMAIL_TEMPLATE);

        //when
        claimantOperationService.notifyCitizen(CLAIM_HWF_AWAITING_RESPONSE, SUBMITTER_NAME, AUTHORISATION);

        //verify
        verify(hwfClaimNotificationService).sendMail(
            CLAIM_HWF_AWAITING_RESPONSE,
            CLAIM_HWF_AWAITING_RESPONSE.getSubmitterEmail(),
            CLAIMANT_EMAIL_TEMPLATE,
            "hwf-claim-update-notification-" + CLAIM_HWF_AWAITING_RESPONSE.getReferenceNumber(),
            SUBMITTER_NAME
        );
    }

    @Test
    public void shouldNotifyCitizenForHwfPending() {
        //given
        given(emailTemplates.getClaimantClaimIssuedWithHwfVerficationPending()).willReturn(CLAIMANT_EMAIL_TEMPLATE);

        //when
        claimantOperationService.notifyCitizen(CLAIM_HWF_PENDING, SUBMITTER_NAME, AUTHORISATION);

        //verify
        verify(hwfClaimNotificationService).sendMail(
            CLAIM_HWF_PENDING,
            CLAIM_HWF_PENDING.getSubmitterEmail(),
            CLAIMANT_EMAIL_TEMPLATE,
            "hwf-claimant-issue-creation-notification-" + CLAIM_HWF_PENDING.getReferenceNumber(),
            SUBMITTER_NAME
        );
    }

    @Test
    public void shouldNotifyCitizenForHwfPendingForMoreInfoRequired() {
        LocalDateTime todaysDateTime = LocalDateTime.now();
        Claim updatedClaim = SampleClaimForHwF.getDefault().toBuilder().respondedAt(todaysDateTime)
            .lastEventTriggeredForHwfCase(MORE_INFO_REQUIRED_FOR_HWF.getValue()).build();

        //when
        claimantOperationService.notifyCitizen(updatedClaim, SUBMITTER_NAME, AUTHORISATION);

        assertThat(updatedClaim.getState().equals(HWF_APPLICATION_PENDING));
    }

    @Test
    public void shouldNotifyCitizenForHwfPendingForMoreInfoRequired1() {
        LocalDateTime todaysDateTime = LocalDateTime.now();
        Claim hwfClaimApplicationPendingStateObj = SampleClaimForHwF.getDefault()
            .toBuilder().respondedAt(todaysDateTime)
            .state(AWAITING_RESPONSE_HWF)
            .lastEventTriggeredForHwfCase(MORE_INFO_REQUIRED_FOR_HWF.getValue()).build();

        //when
        claimantOperationService.notifyCitizen(hwfClaimApplicationPendingStateObj, SUBMITTER_NAME, AUTHORISATION);

        assertThat(hwfClaimApplicationPendingStateObj.getState().equals(AWAITING_RESPONSE_HWF));
    }

    @Test
    public void shouldConfirmRepresentative() {
        //given
        given(emailTemplates.getRepresentativeClaimIssued()).willReturn(REPRESENTATIVE_EMAIL_TEMPLATE);

        //when
        claimantOperationService.confirmRepresentative(CLAIM_HWF_PENDING,
            SUBMITTER_NAME, REPRESENTATIVE_EMAIL, AUTHORISATION);

        //verify
        verify(claimIssuedNotificationService).sendMail(
            eq(CLAIM_HWF_PENDING),
            eq(REPRESENTATIVE_EMAIL),
            any(),
            eq(REPRESENTATIVE_EMAIL_TEMPLATE),
            eq("representative-issue-notification-" + CLAIM_HWF_PENDING.getReferenceNumber()),
            eq(SUBMITTER_NAME)
        );
    }
}
