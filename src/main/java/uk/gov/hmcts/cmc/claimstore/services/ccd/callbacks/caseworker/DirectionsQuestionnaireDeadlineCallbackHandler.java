package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DIRECTIONS_QUESTIONNAIRE_DEADLINE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class DirectionsQuestionnaireDeadlineCallbackHandler extends AbstractStateChangeCallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(DIRECTIONS_QUESTIONNAIRE_DEADLINE);
    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private final CaseDetailsConverter caseDetailsConverter;
    private final boolean ctscEnabled;

    private final ImmutableMap<CallbackType, Callback> callbacks = ImmutableMap.of(
        CallbackType.ABOUT_TO_SUBMIT, this::determineState
    );

    public DirectionsQuestionnaireDeadlineCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.ctscEnabled = ctscEnabled;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        ClaimState state = ctscEnabled ? ClaimState.READY_FOR_PAPER_DQ : ClaimState.OPEN;

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updateState(callbackParams, state))
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    protected CaseDetailsConverter getCaseDetailsConverter() {
        return caseDetailsConverter;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }

}
