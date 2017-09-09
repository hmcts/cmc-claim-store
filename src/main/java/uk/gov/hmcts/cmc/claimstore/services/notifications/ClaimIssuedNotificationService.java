package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.party.NamedParty;
import uk.gov.hmcts.cmc.claimstore.models.party.TitledParty;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.claimstore.utils.PartyUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;

@Service
public class ClaimIssuedNotificationService {
    private final Logger logger = LoggerFactory.getLogger(ClaimIssuedNotificationService.class);

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimIssuedNotificationService(
        final NotificationClient notificationClient,
        final NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(
        final Claim claim,
        final String targetEmail,
        final Optional<String> pin,
        final String emailTemplateId,
        final String reference
    ) {
        final Map<String, String> parameters = aggregateParams(claim, pin);
        try {
            notificationClient.sendEmail(emailTemplateId, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        final NotificationException exception,
        final Claim claim,
        final String targetEmail,
        final Optional<String> pin,
        final String emailTemplateId,
        final String reference
    ) {
        final String errorMessage = "Failure: "
            + " failed to send notification (" + reference
            + " to " + targetEmail + ") "
            + " due to " + exception.getMessage();

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(final Claim claim, final Optional<String> pin) {
        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(CLAIMANT_NAME, getNameWithTitle(claim.getClaimData().getClaimant()));
        parameters.put("claimantType", PartyUtils.getType(claim.getClaimData().getClaimant()));
        if (!claim.getClaimData().isClaimantRepresented()) {
            parameters.put("defendantName", getNameWithTitle(claim.getClaimData().getDefendant()));
        }
        parameters.put("issuedOn", Formatting.formatDate(claim.getIssuedOn()));
        parameters.put("responseDeadline", Formatting.formatDate(claim.getResponseDeadline()));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put("externalId", claim.getExternalId());
        parameters.put("feesPaid", claim.getClaimData().getFeesPaidInPound().toString());
        pin.ifPresent(p -> parameters.put("pin", p));
        return parameters.build();
    }

    private String getNameWithTitle(final NamedParty party) {
        final StringBuilder title = new StringBuilder();
        if (party instanceof TitledParty) {
            ((TitledParty) party).getTitle().ifPresent(t -> title.append(t).append(" "));
        }

        return title.append(party.getName()).toString();
    }

}
