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
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.NamedParty;
import uk.gov.hmcts.cmc.domain.models.party.TitledParty;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FEES_PAID;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.ISSUED_ON;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.PIN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;

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
        final String pin,
        final String emailTemplateId,
        final String reference,
        final String submitterName
    ) {
        final Map<String, String> parameters = aggregateParams(claim, pin, submitterName);
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
        final String pin,
        final String emailTemplateId,
        final String reference,
        final String submitterName
    ) {
        final String errorMessage = "Failure: "
            + " failed to send notification (" + reference
            + " to " + targetEmail + ") "
            + " due to " + exception.getMessage();

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(final Claim claim, final String pin,
                                                final String submitterName) {
        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        if (!claim.getClaimData().isClaimantRepresented()) {
            parameters.put(CLAIMANT_NAME, getNameWithTitle(claim.getClaimData().getClaimant()));
            parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
            parameters.put(DEFENDANT_NAME, getNameWithTitle(claim.getClaimData().getDefendant()));
        } else {
            parameters.put(CLAIMANT_NAME, submitterName);
        }

        parameters.put(ISSUED_ON, Formatting.formatDate(claim.getIssuedOn()));
        parameters.put(RESPONSE_DEADLINE, Formatting.formatDate(claim.getResponseDeadline()));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(FEES_PAID, claim.getClaimData().getFeesPaidInPound().toString());
        Optional.ofNullable(pin).ifPresent(p -> parameters.put(PIN, p));
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
