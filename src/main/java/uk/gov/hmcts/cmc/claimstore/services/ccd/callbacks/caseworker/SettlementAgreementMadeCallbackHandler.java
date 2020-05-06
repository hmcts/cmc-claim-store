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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.*;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

@Service
public class SettlementAgreementMadeCallbackHandler extends AbstractStateChangeCallbackHandler {
    private static final List<CaseEvent> EVENTS =
        Arrays.asList(AGREEMENT_COUNTER_SIGNED_BY_DEFENDANT,
            OFFER_COUNTER_SIGNED_BY_DEFENDANT,
            AGREEMENT_SIGNED_BY_BOTH);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);

    private final boolean ctscEnabled;

    private final ImmutableMap<CallbackType, Callback> callbacks = ImmutableMap.of(
        CallbackType.ABOUT_TO_SUBMIT, this::determineState
    );

    public SettlementAgreementMadeCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled) {

        super(EVENTS, ROLES, caseDetailsConverter);
        this.ctscEnabled = ctscEnabled;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        ClaimState state = ctscEnabled ? ClaimState.SETTLEMENT_AGREEMENT_MADE : ClaimState.OPEN;
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updateState(callbackParams, state))
            .build();
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }
}
