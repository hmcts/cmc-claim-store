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
import uk.gov.hmcts.cmc.claimstore.models.otherparty.TheirDetails;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.party.TitledParty;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.claimstore.utils.PartyTypeContentProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.Map;
import java.util.Optional;

@Service
public class ClaimIssuedNotificationService {
    private final Logger logger = LoggerFactory.getLogger(ClaimIssuedNotificationService.class);

    public static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    public static final String CLAIMANT_TYPE = "claimantType";
    public static final String FRONTEND_BASE_URL = "frontendBaseUrl";
    public static final String EXTERNAL_ID = "externalId";
    public static final String FEES_PAID = "feesPaid";
    public static final String CLAIMANT_NAME = "claimantName";
    public static final String DEFENDANT_NAME = "defendantName";
    public static final String ISSUED_ON = "issuedOn";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    public static final String PIN = "pin";

    private final NotificationClient notificationClient;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public ClaimIssuedNotificationService(
        final NotificationClient notificationClient,
        final NotificationsProperties notificationsProperties) {
        this.notificationClient = notificationClient;
        this.notificationsProperties = notificationsProperties;
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void sendMail(final Claim claim,
                         final String targetEmail,
                         final Optional<String> pin,
                         final String emailTemplateId,
                         final String reference) {
        final Map<String, String> parameters = aggregateParams(claim, pin);
        try {
            notificationClient.sendEmail(emailTemplateId, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(final NotificationException exception,
                                       final Claim claim,
                                       final String targetEmail,
                                       final Optional<String> pin,
                                       final String emailTemplateId,
                                       final String reference) {
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
        parameters.put(CLAIMANT_TYPE, PartyTypeContentProvider.getType(claim.getClaimData().getClaimant()));
        parameters.put(DEFENDANT_NAME, getNameWithTitle(claim.getClaimData().getDefendant()));
        parameters.put(ISSUED_ON, Formatting.formatDate(claim.getIssuedOn()));
        parameters.put(RESPONSE_DEADLINE, Formatting.formatDate(claim.getResponseDeadline()));
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(EXTERNAL_ID, claim.getExternalId());
        parameters.put(FEES_PAID, claim.getClaimData().getFeesPaidInPound().toString());
        pin.ifPresent(p -> parameters.put(PIN, p));
        return parameters.build();
    }

    private String getNameWithTitle(final Party party) {
        final StringBuilder nameWithTitle = new StringBuilder();
        if (party instanceof TitledParty) {
            ((TitledParty)party).getTitle().ifPresent(t -> nameWithTitle.append(t).append(" "));
        }
        return nameWithTitle.append(party.getName()).toString();
    }

    private String getNameWithTitle(final TheirDetails otherParty) {
        final StringBuilder nameWithTitle = new StringBuilder();
        if (otherParty instanceof TitledParty) {
            ((TitledParty)otherParty).getTitle().ifPresent(t -> nameWithTitle.append(t).append(" "));
        }
        return nameWithTitle.append(otherParty.getName()).toString();
    }

}
