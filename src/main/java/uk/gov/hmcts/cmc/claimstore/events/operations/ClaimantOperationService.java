package uk.gov.hmcts.cmc.claimstore.events.operations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.claim.ClaimCreationEventsStatusService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.ClaimIssuedNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.notifications.HwfClaimNotificationService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.SENDING_CLAIMANT_NOTIFICATION;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.AWAITING_RESPONSE_HWF;
import static uk.gov.hmcts.cmc.domain.models.ClaimState.HWF_APPLICATION_PENDING;

@Component
public class ClaimantOperationService {
    private final ClaimIssuedNotificationService claimIssuedNotificationService;
    private final HwfClaimNotificationService hwfClaimNotificationService;
    private final NotificationsProperties notificationsProperties;
    private final ClaimCreationEventsStatusService eventsStatusService;

    @Autowired
    public ClaimantOperationService(
        ClaimIssuedNotificationService claimIssuedNotificationService,
        NotificationsProperties notificationsProperties,
        ClaimCreationEventsStatusService eventsStatusService,
        HwfClaimNotificationService hwfClaimNotificationService
    ) {
        this.claimIssuedNotificationService = claimIssuedNotificationService;
        this.notificationsProperties = notificationsProperties;
        this.eventsStatusService = eventsStatusService;
        this.hwfClaimNotificationService = hwfClaimNotificationService;
    }

    @LogExecutionTime
    public Claim notifyCitizen(Claim claim, String submitterName, String authorisation) {

        if (claim.getState().equals(HWF_APPLICATION_PENDING)) {
            hwfClaimNotificationService.sendMail(
                claim,
                claim.getSubmitterEmail(),
                null,
                notificationsProperties.getTemplates().getEmail().getClaimantClaimIssuedWithHwfVerficationPending(),
                "hwf-claimant-issue-creation-notification-" + claim.getReferenceNumber(),
                submitterName
            );
        } else if (claim.getState().equals(AWAITING_RESPONSE_HWF)) {
            hwfClaimNotificationService.sendMail(
                claim,
                claim.getSubmitterEmail(),
                null,
                notificationsProperties.getTemplates().getEmail().getClaimantHwfUpdate(),
                "hwf-claim-update-notification-" + claim.getReferenceNumber(),
                submitterName
            );
            return claim;
        } else {
            claimIssuedNotificationService.sendMail(
                claim,
                claim.getSubmitterEmail(),
                null,
                notificationsProperties.getTemplates().getEmail().getClaimantClaimIssued(),
                "claimant-issue-notification-" + claim.getReferenceNumber(),
                submitterName
            );
        }
        return eventsStatusService.updateClaimOperationCompletion(authorisation, claim, SENDING_CLAIMANT_NOTIFICATION);
    }

    @LogExecutionTime
    public Claim confirmRepresentative(
        Claim claim,
        String submitterName,
        String representativeEmail,
        String authorisation
    ) {
        claimIssuedNotificationService.sendMail(
            claim,
            representativeEmail,
            null,
            notificationsProperties.getTemplates().getEmail().getRepresentativeClaimIssued(),
            "representative-issue-notification-" + claim.getReferenceNumber(),
            submitterName
        );

        return eventsStatusService.updateClaimOperationCompletion(authorisation, claim, SENDING_CLAIMANT_NOTIFICATION);
    }
}
