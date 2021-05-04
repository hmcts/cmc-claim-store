package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.AGREEMENT_SIGNED_BY_BOTH;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.OFFER_COUNTER_SIGNED_BY_DEFENDANT;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

@Service
public class SettlementAgreementMadeCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS =
        Arrays.asList(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT,
            OFFER_COUNTER_SIGNED_BY_DEFENDANT,
            AGREEMENT_SIGNED_BY_BOTH);
    private static final List<Role> ROLES = List.of(CASEWORKER, CITIZEN);

    private final boolean ctscEnabled;
    private static final String STATE = "state";

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return Map.of(CallbackType.ABOUT_TO_SUBMIT, this::determineState);
    }

    public SettlementAgreementMadeCallbackHandler(
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled) {
        this.ctscEnabled = ctscEnabled;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        ClaimState state = ctscEnabled ? ClaimState.SETTLEMENT_AGREEMENT_MADE : ClaimState.OPEN;
        Map<String, Object> data = new HashMap<>(callbackParams.getRequest().getCaseDetails().getData());
        data.put(STATE, state.getValue());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(data)
            .build();
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
