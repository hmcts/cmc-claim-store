package uk.gov.hmcts.cmc.claimstore.events.revieworder;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ReviewOrder;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.utils.CommonErrors.MISSING_REVIEW_ORDER;
import static uk.gov.hmcts.cmc.domain.utils.EmailUtils.getDefendantEmail;

@Component
public class ReviewOrderCitizenNotificationHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public ReviewOrderCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void onReviewOrderEvent(ReviewOrderEvent event) {
        ReviewOrder reviewOrder = event.getClaim().getReviewOrder()
            .orElseThrow(() -> new IllegalArgumentException(MISSING_REVIEW_ORDER));
        if (null == reviewOrder.getRequestedBy()) {
            throw new IllegalArgumentException("RequestedBy can't be null");
        }
        switch (reviewOrder.getRequestedBy()) {
            case DEFENDANT:
                this.notifyClaimantForReviewOrder(event);
                break;
            case CLAIMANT:
                this.notifyDefendantForReviewOrder(event);
                break;
            default:
                throw new IllegalArgumentException("Invalid requestedBy " + reviewOrder.getRequestedBy());
        }
    }

    public void notifyDefendantForReviewOrder(ReviewOrderEvent event) {
        Claim claim = event.getClaim();

        getDefendantEmail(claim).ifPresent(defendantEmail ->
            notificationService.sendMail(
                defendantEmail,
                notificationsProperties.getTemplates().getEmail().getReviewOrderEmailToDefendant(),
                aggregateParams(claim),
                referenceForDefendant(claim.getReferenceNumber())
            )
        );
    }

    public void notifyClaimantForReviewOrder(ReviewOrderEvent event) {
        Claim claim = event.getClaim();

        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getReviewOrderEmailToClaimant(),
            aggregateParams(claim),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> aggregateParams(Claim claim) {
        return ImmutableMap.<String, String>builder()
            .put("claimReferenceNumber", claim.getReferenceNumber())
            .put("claimantName", claim.getClaimData().getClaimant().getName())
            .put("defendantName", claim.getClaimData().getDefendant().getName())
            .put("frontendBaseUrl", notificationsProperties.getFrontendBaseUrl())
            .build();
    }
}
