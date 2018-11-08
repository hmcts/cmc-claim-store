package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.AgreementCounterSigned;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.CLAIMANT;
import static uk.gov.hmcts.cmc.domain.models.offers.MadeBy.DEFENDANT;

@Component
public class AgreementCounterSignedCitizenActionsHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public AgreementCounterSignedCitizenActionsHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotificationToOtherParty(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        String targetEmail;
        String reference;

        if (event.party == CLAIMANT) {
            targetEmail = claim.getDefendantEmail();
            reference = AgreementCounterSigned.referenceForDefendant(claim.getReferenceNumber(), CLAIMANT.name());
        } else {
            targetEmail = claim.getSubmitterEmail();
            reference = AgreementCounterSigned.referenceForClaimant(claim.getReferenceNumber(), DEFENDANT.name());
        }

        notificationService.sendMail(
            targetEmail,
            notificationsProperties.getTemplates().getEmail().getOfferCounterSignedEmailToOtherParty(),
            aggregateParams(claim),
            reference
        );
    }

    @EventListener
    public void sendNotificationToOfferOriginator(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        String targetEmail;
        String reference;

        if (event.party == CLAIMANT) {
            targetEmail = claim.getSubmitterEmail();
            reference = AgreementCounterSigned.referenceForClaimant(claim.getReferenceNumber(), CLAIMANT.name());
        } else {
            targetEmail = claim.getDefendantEmail();
            reference = AgreementCounterSigned.referenceForDefendant(claim.getReferenceNumber(), DEFENDANT.name());
        }

        notificationService.sendMail(
            targetEmail,
            notificationsProperties.getTemplates().getEmail().getOfferCounterSignedEmailToOriginator(),
            aggregateParams(claim),
            reference
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        Map<String, String> parameters = new HashMap<>();

        // used by originator and other party emails
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        // only used by other party email
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());

        return parameters;
    }
}
