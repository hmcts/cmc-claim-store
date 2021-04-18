package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RoboticsNotificationService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport.RpaEventType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class ResetRpaCallbackHandler extends CallbackHandler {
    private static final String RPA_EVENT_TYPE = "RPAEventType";
    private static final String RPA_STATE_INVALID = "invalid";

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private CaseDetailsConverter caseDetailsConverter;
    private CaseMapper caseMapper;
    private RoboticsNotificationService roboticsNotificationService;

    @Autowired
    public ResetRpaCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper, RoboticsNotificationService roboticsNotificationService) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
        this.roboticsNotificationService = roboticsNotificationService;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
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
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = convertCallbackToClaim(callbackRequest);
        handleRoboticsNotification(callbackRequest, claim.getReferenceNumber());
        Map<String, Object> map = new HashMap<>(caseDetailsConverter.convertToMap(caseMapper.to(claim)));
        AboutToStartOrSubmitCallbackResponse response = AboutToStartOrSubmitCallbackResponse.builder()
            .data(map)
            .build();
        return response;
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
}
