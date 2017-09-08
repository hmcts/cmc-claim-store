package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.exceptions.NotificationException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.DefendantResponse;
import uk.gov.hmcts.cmc.claimstore.models.ResponseData;
import uk.gov.hmcts.cmc.claimstore.models.party.Company;
import uk.gov.hmcts.cmc.claimstore.models.party.Individual;
import uk.gov.hmcts.cmc.claimstore.models.party.Organisation;
import uk.gov.hmcts.cmc.claimstore.models.party.Party;
import uk.gov.hmcts.cmc.claimstore.models.party.SoleTrader;
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.claimstore.utils.Formatting;
import uk.gov.hmcts.cmc.claimstore.utils.PartyTypeContentProvider;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;


@Service
public class DefendantResponseNotificationService {
    private final Logger logger = LoggerFactory.getLogger(DefendantResponseNotificationService.class);

    private static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    private static final String MEDIATION_DECISION_DEADLINE = "mediationDecisionDeadline";
    private static final String FREE_MEDIATION_REQUESTED = "freeMediationRequested";
    private static final String FREE_MEDIATION_NOT_REQUESTED = "freeMediationNotRequested";
    private static final String CLAIMANT_NAME = "claimantName";
    private static final String DEFENDANT_NAME = "defendantName";
    private static final String FRONTEND_BASE_URL = "frontendBaseUrl";
    public static final String CLAIMANT_TYPE = "claimantType";
    public static final String ISSUED_ON = "issuedOn";
    public static final String RESPONSE_DEADLINE = "responseDeadline";
    private final NotificationClient notificationClient;
    private final FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public DefendantResponseNotificationService(
        final NotificationClient notificationClient,
        final FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator,
        final NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.freeMediationDecisionDateCalculator = freeMediationDecisionDateCalculator;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(final Claim claim, final String defendantEmail, final String reference) {
        final Map<String, String> parameters = aggregateParams(claim);

        notify(defendantEmail, getDefendantResponseIssuedEmailTemplate(claim), parameters, reference);
    }

    private String getDefendantResponseIssuedEmailTemplate(final Claim claim) {
        final Party party = claim.getClaimData().getClaimant();

        if (party instanceof Individual || party instanceof SoleTrader) {
            return getEmailTemplates().getDefendantResponseIssuedToIndividual();
        } else if (party instanceof Company || party instanceof Organisation) {
            return getEmailTemplates().getDefendantResponseIssuedToCompany();
        } else {
            throw new NotificationException(("Unknown claimant type " + party));
        }
    }

    public void notifyClaimant(
        final Claim claim,
        final DefendantResponse response,
        final String submitterEmail,
        final String reference
    ) {
        final Map<String, String> parameters = aggregateParams(claim, response.getResponse());
        notify(submitterEmail, getEmailTemplates().getClaimantResponseIssued(), parameters, reference);
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void notify(
        final String targetEmail,
        final String emailTemplate,
        final Map<String, String> parameters,
        final String reference
    ) {

        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        final NotificationException exception,
        final Claim claim,
        final String targetEmail,
        final String emailTemplate,
        final String reference
    ) {
        final String errorMessage = String.format(
            "Failure: failed to send notification ( %s to %s ) due to %s",
            reference, targetEmail, exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(final Claim claim) {

        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(CLAIMANT_TYPE, PartyTypeContentProvider.getType(claim.getClaimData().getClaimant()));
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        return parameters.build();
    }

    private Map<String, String> aggregateParams(final Claim claim, final ResponseData responseData) {
        boolean isFreeMediationRequested = responseData.getFreeMediation().equals(ResponseData.FreeMediationOption.YES);
        LocalDate decisionDeadline = freeMediationDecisionDateCalculator.calculateDecisionDate(LocalDate.now());

        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(CLAIMANT_TYPE, PartyTypeContentProvider.getType(claim.getClaimData().getClaimant()));
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(MEDIATION_DECISION_DEADLINE, formatDate(decisionDeadline));
        parameters.put(FREE_MEDIATION_REQUESTED, isFreeMediationRequested ? "yes" : "");
        parameters.put(FREE_MEDIATION_NOT_REQUESTED, !isFreeMediationRequested ? "yes" : "");
        parameters.put(ISSUED_ON, Formatting.formatDate(claim.getIssuedOn()));
        parameters.put(RESPONSE_DEADLINE, Formatting.formatDate(claim.getResponseDeadline()));

        return parameters.build();
    }

    private EmailTemplates getEmailTemplates() {
        final NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
