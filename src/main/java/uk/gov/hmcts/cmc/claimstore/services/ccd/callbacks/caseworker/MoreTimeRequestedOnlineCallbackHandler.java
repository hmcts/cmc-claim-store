package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.PartyUtils;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_ONLINE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForClaimant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationReferenceBuilder.MoreTimeRequested.referenceForDefendant;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIMANT_TYPE;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.DEFENDANT_NAME;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.FRONTEND_BASE_URL;
import static uk.gov.hmcts.cmc.claimstore.services.notifications.content.NotificationTemplateParameters.RESPONSE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
public class MoreTimeRequestedOnlineCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);
    private static final List<CaseEvent> EVENTS = Collections.singletonList(MORE_TIME_REQUESTED_ONLINE);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationService notificationService;
    private final NotificationsProperties notificationsProperties;
    private final UserService userService;

    @Autowired
    public MoreTimeRequestedOnlineCallbackHandler(
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        CaseDetailsConverter caseDetailsConverter,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties,
        UserService userService
    ) {
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationService = notificationService;
        this.notificationsProperties = notificationsProperties;
        this.userService = userService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_START, this::calculateResponseDeadline,
            CallbackType.ABOUT_TO_SUBMIT, this::performPostProcesses
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse calculateResponseDeadline(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        var builder = AboutToStartOrSubmitCallbackResponse.builder();

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue();
        LocalDate issuedOnDate = null;
        Optional<LocalDate> claimIssuedOnDate = claim.getIssuedOn();
        if (claimIssuedOnDate.isPresent()) {
            issuedOnDate = claimIssuedOnDate.get();
        }
        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(
            respondent.getPaperFormIssueDate() != null
                ? respondent.getPaperFormIssueDate() : issuedOnDate);
        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, newDeadline);

        if (!validationResult.isEmpty()) {
            return builder.errors(validationResult).build();
        }
        builder.data(caseDetailsConverter.convertToMap(ccdCase));
        return builder.build();
    }

    private CallbackResponse performPostProcesses(CallbackParams callbackParams) {
        var builder = AboutToStartOrSubmitCallbackResponse.builder();

        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        sendNotificationToClaimant(claim);

        if (claim.getDefendantEmail() != null && claim.getDefendantId() != null) {
            sendEmailNotificationToDefendant(claim);
        }
        return builder.data(caseDetailsConverter.convertToMap(ccdCase)).build();
    }

    private void sendEmailNotificationToDefendant(Claim claim) {
        notificationService.sendMail(
            claim.getDefendantEmail(),
            notificationsProperties.getTemplates().getEmail().getDefendantMoreTimeRequested(),
            prepareNotificationParameters(claim),
            referenceForDefendant(claim.getReferenceNumber())
        );
    }

    private void sendNotificationToClaimant(Claim claim) {
        notificationService.sendMail(
            claim.getSubmitterEmail(),
            notificationsProperties.getTemplates().getEmail().getClaimantMoreTimeRequested(),
            prepareNotificationParameters(claim),
            referenceForClaimant(claim.getReferenceNumber())
        );
    }

    private Map<String, String> prepareNotificationParameters(Claim claim) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, claim.getReferenceNumber(),
            CLAIMANT_TYPE, PartyUtils.getType(claim.getClaimData().getClaimant()),
            CLAIMANT_NAME, claim.getClaimData().getClaimant().getName(),
            DEFENDANT_NAME, claim.getClaimData().getDefendant().getName(),
            RESPONSE_DEADLINE, formatDate(claim.getResponseDeadline()),
            FRONTEND_BASE_URL, notificationsProperties.getFrontendBaseUrl()
        );
    }
}
