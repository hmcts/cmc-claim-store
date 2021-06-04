package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RoboticsNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RpaEventType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.BreathingSpace;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RpaEventType.BREATHING_SPACE_ENTERED;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RpaEventType.BREATHING_SPACE_LIFTED;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class ResetRpaCallbackHandler extends CallbackHandler {
    private static final String RPA_EVENT_TYPE = "RPAEventType";
    private static final String RPA_STATE_INVALID = "invalid";

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private CaseDetailsConverter caseDetailsConverter;
    private RoboticsNotificationService roboticsNotificationService;

    @Autowired
    public ResetRpaCallbackHandler(
        CaseDetailsConverter caseDetailsConverter, RoboticsNotificationService roboticsNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.roboticsNotificationService = roboticsNotificationService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(
            CallbackType.ABOUT_TO_SUBMIT, this::requestResetRpa
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return Collections.singletonList(CaseEvent.RESEND_RPA);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse requestResetRpa(CallbackParams callbackParams) {
        final var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = convertCallbackToClaim(callbackRequest);
        String validationMessage = validateBreathingSpaceEvents(callbackRequest, claim);
        if (validationMessage != null) {
            List<String> errors = new ArrayList<>();
            errors.add(validationMessage);
            responseBuilder.errors(errors);
        } else {
            handleRoboticsNotification(callbackRequest, claim.getReferenceNumber());
            var caseDetails = callbackParams.getRequest().getCaseDetails();
            var ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
            responseBuilder.data(caseDetailsConverter.convertToMap(ccdCase));
        }
        return responseBuilder.build();
    }

    private Claim convertCallbackToClaim(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }

    private String handleRoboticsNotification(CallbackRequest callbackRequest, String referenceNumber) {
        switch (RpaEventType.fromValue((String) (callbackRequest.getCaseDetails()
            .getData().get(RPA_EVENT_TYPE)))) {
            case CLAIM:
                return roboticsNotificationService.rpaClaimNotification(referenceNumber);
            case MORE_TIME:
                return roboticsNotificationService.rpaMoreTimeNotifications(referenceNumber);
            case CCJ:
                return roboticsNotificationService.rpaCCJNotifications(referenceNumber);
            case DEFENDANT_RESPONSE:
                return roboticsNotificationService.rpaResponseNotifications(referenceNumber);
            case PAID_IN_FULL:
                return roboticsNotificationService.rpaPIFNotifications(referenceNumber);
            case BREATHING_SPACE_ENTERED:
                return roboticsNotificationService.rpaEnterBreathingSpaceNotifications(referenceNumber);
            case BREATHING_SPACE_LIFTED:
                return roboticsNotificationService.rpaLiftBreathingSpaceNotifications(referenceNumber);
            default:
                throw new BadRequestException(RPA_STATE_INVALID);
        }
    }

    private String validateBreathingSpaceEvents(CallbackRequest callbackRequest, Claim claim) {

        if (callbackRequest.getCaseDetails().getData().get(RPA_EVENT_TYPE)
            .equals(BREATHING_SPACE_ENTERED.name())) {
            if (!(claim.getClaimData().getBreathingSpace().isPresent())) {
                return "This claim is still not entered into Breathing space";
            }
        } else if (callbackRequest.getCaseDetails().getData().get(RPA_EVENT_TYPE)
            .equals(BREATHING_SPACE_LIFTED.name())) {
            Optional<BreathingSpace> breathingSpaceOptional = claim.getClaimData().getBreathingSpace();
            if ((breathingSpaceOptional.isPresent())) {
                if (breathingSpaceOptional.get().getBsLiftedFlag().equals("No")) {
                    return "This claim is still not lifted its Breathing space";
                }
            } else {
                return "This claim is still not entered into Breathing space";
            }
        }
        return null;
    }
}
