package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.breathingspace;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDBreathingSpace;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.utils.DocumentNameUtils.buildBreathingSpaceLiftedFileBaseName;

@Service
public class BreathingSpaceLiftedCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Arrays.asList(CASEWORKER, CITIZEN);
    private static final List<CaseEvent> EVENTS = Arrays.asList(CaseEvent.BREATHING_SPACE_LIFTED);
    private final CaseDetailsConverter caseDetailsConverter;
    private final NotificationsProperties notificationsProperties;
    private final String breathingSpaceLiftedTemplateID;
    private final EventProducer eventProducer;
    private final UserService userService;
    private final BreathingSpaceLetterService breathingSpaceLetterService;
    private final BreathingSpaceEmailService breathingSpaceEmailService;

    private String validationMessage;

    @Autowired
    public BreathingSpaceLiftedCallbackHandler(CaseDetailsConverter caseDetailsConverter,
                                               NotificationsProperties notificationsProperties,
                                               @Value("${doc_assembly.breathingSpaceLiftedTemplateID}")
                                                   String breathingSpaceLiftedTemplateID,
                                               EventProducer eventProducer, UserService userService,
                                               BreathingSpaceLetterService breathingSpaceLetterService,
                                               BreathingSpaceEmailService breathingSpaceEmailService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.notificationsProperties = notificationsProperties;
        this.breathingSpaceLiftedTemplateID = breathingSpaceLiftedTemplateID;
        this.eventProducer = eventProducer;
        this.userService = userService;
        this.breathingSpaceLetterService = breathingSpaceLetterService;
        this.breathingSpaceEmailService = breathingSpaceEmailService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::breathingSpaceLiftedAboutToStartCallBack,
            CallbackType.MID, this::breathingSpaceLiftedMidCallBack,
            CallbackType.ABOUT_TO_SUBMIT, this::breathingSpaceLiftedAboutToSubmitCallBack,
            CallbackType.SUBMITTED, this::breathingSpaceLiftedPostOperations
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

    private CallbackResponse breathingSpaceLiftedAboutToStartCallBack(CallbackParams callbackParams) {
        validationMessage = null;
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        validateBreathingSpaceDetails(ccdCase, CallbackType.ABOUT_TO_START);
        List<String> errors = new ArrayList<>();
        if (null != validationMessage) {
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        }
        return responseBuilder.build();
    }

    private CallbackResponse breathingSpaceLiftedMidCallBack(CallbackParams callbackParams) {
        validationMessage = null;
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        List<String> errors = new ArrayList<>();
        validateBreathingSpaceDetails(ccdCase, CallbackType.MID);
        if (null != validationMessage) {
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        } else {
            CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();
            if (ccdBreathingSpace.getBsLiftedDateByInsolvencyTeam() != null) {
                ccdBreathingSpace.setBsLiftedDateByInsolvencyTeamTemp(null);
            } else {
                ccdBreathingSpace.setBsLiftedDateByInsolvencyTeamTemp(" ");
            }
            responseBuilder.data(Map.of("breathingSpaceSummary", ccdBreathingSpace));
        }
        return responseBuilder.build();
    }

    private void validateBreathingSpaceDetails(CCDCase ccdCase, CallbackType callbackType) {
        if (ccdCase.getBreathingSpace() != null) {
            //validationMessage = validateBreathingSpaceDetailsInCCDCase(ccdCase, callbackType);
        } else {
            if (callbackType.equals(CallbackType.ABOUT_TO_START)
                && (ccdCase.getState().equals(ClaimState.TRANSFERRED.getValue())
                || ccdCase.getState().equals(ClaimState.BUSINESS_QUEUE.getValue())
                || ccdCase.getState().equals(ClaimState.HWF_APPLICATION_PENDING.getValue())
                || ccdCase.getState().equals(ClaimState.SETTLED.getValue())
                || ccdCase.getState().equals(ClaimState.CLOSED.getValue())
                || ccdCase.getState().equals(ClaimState.PROCEEDS_IN_CASE_MAN.getValue())
                || ccdCase.getState().equals(ClaimState.AWAITING_RESPONSE_HWF.getValue())
                || ccdCase.getState().equals(ClaimState.CLOSED_HWF.getValue()))) {
                validationMessage = "This Event cannot be triggered "
                    + "since the claim is no longer part of the online civil money claims journey";
            }
        }
    }

    private String validateBreathingSpaceDetailsInCCDCase(CCDCase ccdCase, CallbackType callbackType) {
        if (callbackType.equals(CallbackType.ABOUT_TO_START)) {
            validationMessage = "Breathing Space is already entered for this Claim";
        } else {
            CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();
            if (ccdBreathingSpace.getBsReferenceNumber() != null
                && ccdBreathingSpace.getBsReferenceNumber().length() > 16) {
                validationMessage = "The reference number must be maximum of 16 Characters";
            } else if (ccdBreathingSpace.getBsEnteredDateByInsolvencyTeam() != null
                && ccdBreathingSpace.getBsEnteredDateByInsolvencyTeam().isAfter(LocalDate.now())) {
                validationMessage = "The start date must not be after today's date";
            } else if (ccdBreathingSpace.getBsExpectedEndDate() != null
                && ccdBreathingSpace.getBsExpectedEndDate().isBefore(LocalDate.now())) {
                validationMessage = "The expected end date must not be before today's date";
            }
        }
        return validationMessage;
    }

    private CallbackResponse breathingSpaceLiftedAboutToSubmitCallBack(CallbackParams callbackParams) {

        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        if (ccdCase.getBreathingSpace() != null) {
            CCDBreathingSpace ccdBreathingSpace = ccdCase.getBreathingSpace();
            ccdBreathingSpace.setBsEnteredDate(LocalDate.now());
            ccdCase.setBreathingSpace(ccdBreathingSpace);
        }
        CCDCase updatedCase = ccdCase;
        if (userService.getUserDetails(authorisation).isCaseworker()) {
            Claim claim = caseDetailsConverter.extractClaim(caseDetails);
            breathingSpaceEmailService.sendNotificationToClaimant(claim,
                notificationsProperties.getTemplates().getEmail().getBreathingSpaceLiftedEmailToClaimant());
            if (isDefendentLinked(claim)) {
                breathingSpaceEmailService.sendEmailNotificationToDefendant(claim,
                    notificationsProperties.getTemplates().getEmail().getBreathingSpaceLiftedEmailToDefendant());
            } else {
                updatedCase = breathingSpaceLetterService.sendLetterToDefendantFomCCD(ccdCase, claim, authorisation,
                    breathingSpaceLiftedTemplateID,
                    buildBreathingSpaceLiftedFileBaseName(ccdCase.getPreviousServiceCaseReference(), true));
            }
        }
        var builder = AboutToStartOrSubmitCallbackResponse.builder();
        return builder.data(caseDetailsConverter.convertToMap(updatedCase)).build();
    }

    private CallbackResponse breathingSpaceLiftedPostOperations(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(CallbackParams.Params.BEARER_TOKEN).toString();
        final CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        eventProducer.createBreathingSpaceLiftedEvent(
            claim,
            ccdCase,
            authorisation,
            breathingSpaceLiftedTemplateID,
            notificationsProperties.getTemplates().getEmail().getBreathingSpaceLiftedEmailToClaimant(),
            notificationsProperties.getTemplates().getEmail().getBreathingSpaceLiftedEmailToDefendant(),
            !userService.getUserDetails(authorisation).isCaseworker(),
            true
        );
        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isDefendentLinked(Claim claim) {
        return !StringUtils.isBlank(claim.getDefendantId());
    }
}
