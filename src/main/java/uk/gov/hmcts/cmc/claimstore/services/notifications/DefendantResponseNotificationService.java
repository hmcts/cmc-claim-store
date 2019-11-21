package uk.gov.hmcts.cmc.claimstore.services.notifications;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.EmailTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationTemplates;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.services.FreeMediationDecisionDateCalculator;
import uk.gov.hmcts.cmc.claimstore.utils.DirectionsQuestionnaireUtils;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;

import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.ResponseHelper.admissionResponse;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.hasDefendantOptedForMediation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isFullDefence;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isFullDefenceDisputeAndNoMediation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isPartAdmission;

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
    private static final String INTENTION_TO_PROCEED_DEADLINE = "intentionToProceedDeadline";
    private static final int INTENTION_TO_PROCEED_LIMIT = 33;
    private final NotificationService notificationService;
    private final FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator;
    private final NotificationsProperties notificationsProperties;

    @Autowired
    public DefendantResponseNotificationService(
        NotificationService notificationService,
        FreeMediationDecisionDateCalculator freeMediationDecisionDateCalculator,
        NotificationsProperties notificationsProperties
    ) {
        this.notificationService = notificationService;
        this.freeMediationDecisionDateCalculator = freeMediationDecisionDateCalculator;
        this.notificationsProperties = notificationsProperties;
    }

    public void notifyDefendant(Claim claim, String defendantEmail, String reference) {
        Map<String, String> parameters = aggregateParams(claim);

        String template = getDefendantResponseIssuedEmailTemplate(claim);
        notificationService.sendMail(defendantEmail, template, parameters, reference);
    }

    private String getDefendantResponseIssuedEmailTemplate(Claim claim) {
        Response response = claim.getResponse().orElseThrow(() -> new IllegalStateException("Response expected"));

        if (isOnlineDqWithNoMediationAndHasEitherFullDefenceOrPartAdmission(claim, response)) {
            return getEmailTemplates().getDefendantResponseForDqPilotWithNoMediationIssued();
        }

        if (isFullDefenceDisputeAndNoMediation(response)) {
            return getEmailTemplates().getDefendantResponseWithNoMediationIssued();
        } else {
            return getEmailTemplates().getDefendantResponseIssued();
        }
    }

    private boolean isOnlineDqWithNoMediationAndHasEitherFullDefenceOrPartAdmission(Claim claim, Response response) {
        return DirectionsQuestionnaireUtils.isOnlineDQ(claim)
            && !hasDefendantOptedForMediation(response)
            && (isFullDefence(response) || isPartAdmission(response));
    }

    public void notifyClaimant(
        Claim claim,
        String reference
    ) {
        Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
        Map<String, String> parameters = aggregateParams(claim, response);

        String emailTemplate = getClaimantEmailTemplate(claim, response);

        notificationService.sendMail(claim.getSubmitterEmail(), emailTemplate, parameters, reference);
    }

    private String getClaimantEmailTemplate(Claim claim, Response response) {

        if (isOnlineDqWithNoMediationAndHasEitherFullDefenceOrPartAdmission(claim, response)) {
            return getEmailTemplates().getClaimantResponseForDqPilotWithNoMediationIssued();
        }

        if (admissionResponse(response)) {
            return getEmailTemplates().getDefendantAdmissionResponseToClaimant();
        }

        YesNoOption mediation = response.getFreeMediation().orElse(YesNoOption.YES);
        if (mediation == YesNoOption.YES) {
            return getEmailTemplates().getClaimantResponseWithMediationIssued();
        }

        return getEmailTemplates().getClaimantResponseIssued();

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

        parameters.put(INTENTION_TO_PROCEED_DEADLINE, formatDate(claim.getRespondedAt()
            .plusDays(INTENTION_TO_PROCEED_LIMIT)
            .toLocalDate()));

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
        parameters.put(INTENTION_TO_PROCEED_DEADLINE, formatDate(claim.getRespondedAt()
            .plusDays(INTENTION_TO_PROCEED_LIMIT)
            .toLocalDate()));

        return parameters.build();
    }

    private EmailTemplates getEmailTemplates() {
        NotificationTemplates templates = notificationsProperties.getTemplates();
        return templates.getEmail();
    }
}
