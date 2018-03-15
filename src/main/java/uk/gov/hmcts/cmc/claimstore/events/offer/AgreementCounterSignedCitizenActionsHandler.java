package uk.gov.hmcts.cmc.claimstore.events.offer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.AgreementCounterSigned;
import uk.gov.hmcts.cmc.claimstore.services.notifications.OfferMadeNotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.offers.MadeBy;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.COUNTER_SIGNING_PARTY;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Component
public class AgreementCounterSignedCitizenActionsHandler {

    private final OfferMadeNotificationService offerMadeNotificationService;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public AgreementCounterSignedCitizenActionsHandler(
        OfferMadeNotificationService offerMadeNotificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.offerMadeNotificationService = offerMadeNotificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void sendNotificationToOtherParty(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        String targetEmail = null;
        String reference = null;

        if (event.party.equals(MadeBy.CLAIMANT)) {
            targetEmail = claim.getDefendantEmail();
            reference = AgreementCounterSigned.referenceForDefendant(claim.getReferenceNumber());
        } else {
            targetEmail = claim.getSubmitterEmail();
            reference = AgreementCounterSigned.referenceForClaimant(claim.getReferenceNumber());
        }

        offerMadeNotificationService.sendNotificationEmail(
            targetEmail,
            notificationsProperties.getTemplates().getEmail().getOfferCounterSignedByOtherParty(),
            aggregateParams(claim, event.party),
            reference
        );

    }

    @EventListener
    public void sendNotificationToOfferOriginator(AgreementCountersignedEvent event) {
        Claim claim = event.getClaim();
        String targetEmail = null;
        String reference = null;


        if (event.party.equals(MadeBy.CLAIMANT)) {
            targetEmail = claim.getSubmitterEmail();
            reference = AgreementCounterSigned.referenceForDefendant(claim.getReferenceNumber());
        } else {
            targetEmail = claim.getDefendantEmail();
            reference = AgreementCounterSigned.referenceForClaimant(claim.getReferenceNumber());
        }

        offerMadeNotificationService.sendNotificationEmail(
            targetEmail,
            notificationsProperties.getTemplates().getEmail().getOfferCounterSignedByOriginator(),
            aggregateParams(claim, event.party),
            reference
        );

    }

    private Map<String, String> aggregateParams(Claim claim, MadeBy madeBy) {

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(COUNTER_SIGNING_PARTY, madeBy.name());

        return parameters;
    }
}
