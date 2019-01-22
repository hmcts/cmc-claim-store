package uk.gov.hmcts.cmc.claimstore.events.paidinfull;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.flywaydb.core.internal.logging.console.ConsoleLog;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;

import javax.swing.text.html.Option;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.PaidInFull.referenceForDefendant;

@Component
public class PaidInFullCitizenNotificationHandler {

    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;

    public PaidInFullCitizenNotificationHandler(
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
    }

    @EventListener
    public void onPaidInFullEvent(PaidInFullEvent event) {
        this.notifyDefendantForPaidInFull(event);
    }

    public void notifyDefendantForPaidInFull(PaidInFullEvent event) {
        Claim claim = event.getClaim();
        Map<String, String> parameters = aggregateParams(claim);

        getDefendantEmail(claim).ifPresent(defendantEmail ->
            notificationService.sendMail(
                defendantEmail,
                notificationsProperties.getTemplates().getEmail().getClaimantSaysDefendantHasPaidInFull(),
                parameters,
                referenceForDefendant(claim.getReferenceNumber())
            )
        );
    }

    private Optional<String> getDefendantEmail(Claim claim) {
        if (StringUtils.isNotBlank(claim.getDefendantEmail())) {
            return Optional.ofNullable(claim.getDefendantEmail());
        } else {
            return claim.getClaimData().getDefendant().getEmail();
        }
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
