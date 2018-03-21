package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.AgreementCounterSigned;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.COUNTER_SIGNING_PARTY;
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
            aggregateParams(claim, event.party),
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
            aggregateParams(claim, event.party),
            reference
        );
    }

    private Map<String, String> aggregateParams(Claim claim, MadeBy madeBy) {
        String counterSignParty = madeBy == CLAIMANT
            ? claim.getClaimData().getClaimant().getName()
            : claim.getClaimData().getDefendant().getName();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(COUNTER_SIGNING_PARTY, counterSignParty);

        return parameters;
    }
}
