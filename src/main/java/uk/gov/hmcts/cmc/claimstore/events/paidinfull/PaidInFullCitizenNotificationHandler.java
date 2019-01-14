package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import com.google.common.collect.ImmutableMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;

@Component
public class PaidInFullCitizenNotificationHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final AppInsights appInsights;

    public PaidInFullCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        AppInsights appInsights
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.appInsights = appInsights;
    }

    @EventListener
    public void onPaidInFullEvent(PaidInFullEvent event) {
        this.notifyDefendantForPaidInFull(event);
        this.paidInFullAppInsight(event);
    }

    public void notifyDefendantForPaidInFull(PaidInFullEvent event) {
        Claim claim = event.getClaim();
        Map<String, String> parameters = aggregateParams(claim);
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantSaysDefendantHasPaidInFull(),
            parameters,
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private void paidInFullAppInsight(PaidInFullEvent event) {
        Claim claim = event.getClaim();
        appInsights.trackEvent(AppInsightsEvent.PAID_IN_FULL, REFERENCE_NUMBER, claim.getReferenceNumber());
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
