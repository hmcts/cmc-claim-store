package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

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

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

@Service
public class InterlocutoryJudgmentCallbackHandler extends AbstractStateChangeCallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INTERLOCUTORY_JUDGMENT);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);

    private final boolean ctscEnabled;

    private final Map<CallbackType, Callback> callbacks = Map.of(
        CallbackType.ABOUT_TO_SUBMIT, this::determineState
    );

    public InterlocutoryJudgmentCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled) {

        super(EVENTS, ROLES, caseDetailsConverter);
        this.ctscEnabled = ctscEnabled;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        ClaimState state = ctscEnabled ? ClaimState.JUDGMENT_DECIDE_AMOUNT : ClaimState.OPEN;
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updateState(callbackParams, state))
            .build();
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }

}
