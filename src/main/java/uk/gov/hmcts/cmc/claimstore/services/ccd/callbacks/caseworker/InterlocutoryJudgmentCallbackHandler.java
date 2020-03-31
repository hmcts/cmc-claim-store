package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCUTORY_JUDGMENT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

public class InterlocutoryJudgmentCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(INTERLOCUTORY_JUDGMENT);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);
    private static final String STATE = "state";

    private final boolean ctscEnabled;

    private final ImmutableMap<CallbackType, Callback> callbacks = ImmutableMap.of(
        CallbackType.ABOUT_TO_SUBMIT, this::determineState
    );

    public InterlocutoryJudgmentCallbackHandler(@Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled) {
        this.ctscEnabled = ctscEnabled;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        ClaimState state = ctscEnabled ? ClaimState.JUDGMENT_DECIDE_AMOUNT : ClaimState.OPEN;

        Map<String, Object> data = new HashMap<>(callbackParams.getRequest().getCaseDetails().getData());
        data.put(STATE, state.getValue());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }
}
