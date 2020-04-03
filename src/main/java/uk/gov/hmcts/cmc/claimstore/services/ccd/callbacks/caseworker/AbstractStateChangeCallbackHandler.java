package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.HashMap;
import java.util.Map;

abstract class AbstractStateChangeCallbackHandler extends CallbackHandler {
    private static final String STATE = "state";

    Map<String, Object> updateState(CallbackParams callbackParams, ClaimState state) {

        Map<String, Object> data = new HashMap<>(callbackParams.getRequest().getCaseDetails().getData());
        data.put(STATE, state.getValue());

        return data;
    }

}
