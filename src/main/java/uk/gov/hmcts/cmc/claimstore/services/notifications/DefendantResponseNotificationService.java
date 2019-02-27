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
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.domain.exceptions.NotificationException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.party.Company;
import uk.gov.hmcts.cmc.domain.models.party.Individual;
import uk.gov.hmcts.cmc.domain.models.party.Organisation;
import uk.gov.hmcts.cmc.domain.models.party.Party;
import uk.gov.hmcts.cmc.domain.models.party.SoleTrader;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper.admissionResponse;

@Service
public class DefendantResponseNotificationService {
    public static final String DQS_DEADLINE = "DQsdeadline";
    private static final String CLAIM_REFERENCE_NUMBER = "claimReferenceNumber";
    private static final String MEDIATION_DECISION_DEADLINE = "mediationDecisionDeadline";
    private static final String FREE_MEDIATION_REQUESTED = "freeMediationRequested";
    private static final String FREE_MEDIATION_NOT_REQUESTED = "freeMediationNotRequested";
    private static final String CLAIMANT_NAME = "claimantName";
    private static final String DEFENDANT_NAME = "defendantName";
    private static final String FRONTEND_BASE_URL = "frontendBaseUrl";
    private static final String CLAIMANT_TYPE = "claimantType";
    private static final String ISSUED_ON = "issuedOn";
    private static final String RESPONSE_DEADLINE = "responseDeadline";
    private static final Logger logger = LoggerFactory.getLogger(DefendantResponseNotificationService.class);
    private final NotificationClient notificationClient;
    private final FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public DefendantResponseNotificationService(
        NotificationClient notificationClient,
        FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationClient = notificationClient;
        this.freeMediationDecisionDateCalculator = freeMediationDecisionDateCalculator;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(Claim claim, String defendantEmail, String reference) {
        Map<String, String> parameters = aggregateParams(claim);

        String template = getDefendantResponseIssuedEmailTemplate(claim);
        notify(defendantEmail, template, parameters, reference);
    }

    private String getDefendantResponseIssuedEmailTemplate(Claim claim) {
        Party party = claim.getClaimData().getClaimant();
        Response response = claim.getResponse().orElseThrow(() -> new IllegalStateException("Response expected"));

        if (isFullDefenceAndNoMediation(response)) {
            return getEmailTemplates().getDefendantResponseWithNoMediationIssued();
        }

        if (party instanceof Individual || party instanceof SoleTrader) {
            return getEmailTemplates().getDefendantResponseIssuedToIndividual();
        } else if (party instanceof Company || party instanceof Organisation) {
            return getEmailTemplates().getDefendantResponseIssuedToCompany();
        } else {
            throw new NotificationException(("Unknown claimant type " + party));
        }
    }

    private boolean isFullDefenceAndNoMediation(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE)
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }

    public void notifyClaimant(
        Claim claim,
        String reference
    ) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        Map<String, String> parameters = aggregateParams(claim, response);

        String emailTemplate = getClaimantEmailTemplate(response);

        notify(claim.getSubmitterEmail(), emailTemplate, parameters, reference);
    }

    private String getClaimantEmailTemplate(Response response) {
        YesNoOption mediation = response.getFreeMediation().orElse(YesNoOption.YES);
        if (admissionResponse(response)) {
            return getEmailTemplates().getDefendantAdmissionResponseToClaimant();
        }
        if (mediation == YesNoOption.YES) {
            return getEmailTemplates().getClaimantResponseWithMediationIssued();
        } else {
            if (isFullDefenceAndNoMediation(response)) {
                return getEmailTemplates().getClaimantResponseWithNoMediationIssued();
            }
            return getEmailTemplates().getClaimantResponseIssued();
        }
    }

    @Retryable(value = NotificationException.class, backoff = @Backoff(delay = 200))
    public void notify(
        String targetEmail,
        String emailTemplate,
        Map<String, String> parameters,
        String reference
    ) {
        try {
            notificationClient.sendEmail(emailTemplate, targetEmail, parameters, reference);
        } catch (NotificationClientException e) {
            throw new NotificationException(e);
        }
    }

    @Recover
    public void logNotificationFailure(
        NotificationException exception,
        Claim claim,
        String targetEmail,
        String emailTemplate,
        String reference
    ) {
        String errorMessage = String.format(
            "Failure: failed to send notification (%s) due to %s",
            reference, exception.getMessage()
        );

        logger.info(errorMessage, exception);
    }

    private Map<String, String> aggregateParams(Claim claim) {

        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());

        Response response = claim.getResponse().orElse(null);
        Objects.requireNonNull(response);

        if (isFullDefenceAndNoMediation(response)) {
            parameters.put(DQS_DEADLINE, formatDate(claim.getDirectionsQuestionnaireDeadline()));
        }

        return parameters.build();
    }

    private Map<String, String> aggregateParams(Claim claim, Response response) {
        boolean isFreeMediationApplicable = response.getFreeMediation().isPresent();
        boolean isFreeMediationRequested = response.getFreeMediation()
            .orElse(YesNoOption.NO).equals(YesNoOption.YES);

        LocalDate decisionDeadline = freeMediationDecisionDateCalculator.calculateDecisionDate(LocalDate.now());

        ImmutableMap.Builder<String, String> parameters = new ImmutableMap.Builder<>();
        parameters.put(CLAIMANT_NAME, claim.getClaimData().getClaimant().getName());
        parameters.put(CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()));
        parameters.put(DEFENDANT_NAME, claim.getClaimData().getDefendant().getName());
        parameters.put(FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl());
        parameters.put(CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber());
        parameters.put(MEDIATION_DECISION_DEADLINE, formatDate(decisionDeadline));
        parameters.put(FREE_MEDIATION_REQUESTED, isFreeMediationApplicable && isFreeMediationRequested ? "yes" : "");
        parameters.put(
            FREE_MEDIATION_NOT_REQUESTED, isFreeMediationApplicable && !isFreeMediationRequested ? "yes" : ""
        );
        parameters.put(ISSUED_ON, formatDate(claim.getIssuedOn()));
        parameters.put(RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()));

        if (isFullDefenceAndNoMediation(response)) {
            parameters.put(DQS_DEADLINE, formatDate(claim.getDirectionsQuestionnaireDeadline()));
        }

        return parameters.build();
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
