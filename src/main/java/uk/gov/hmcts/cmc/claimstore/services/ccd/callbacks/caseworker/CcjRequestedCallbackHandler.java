package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.CCJ_REQUESTED;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;

@Service
public class CcjRequestedCallbackHandler extends CallbackHandler {
    private static final List<CaseEvent> EVENTS = Collections.singletonList(CCJ_REQUESTED);
    private static final List<Role> ROLES = Collections.singletonList(CITIZEN);
    private static final String STATE = "state";
    private final CaseDetailsConverter caseDetailsConverter;
    private final CaseMapper caseMapper;
    private final boolean ctscEnabled;

    private final ImmutableMap<CallbackType, Callback> callbacks = ImmutableMap.of(
        CallbackType.ABOUT_TO_SUBMIT, this::determineState
    );

    public CcjRequestedCallbackHandler(
        @Value("${feature_toggles.ctsc_enabled}") boolean ctscEnabled,
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper) {
        this.ctscEnabled = ctscEnabled;
        this.caseDetailsConverter = caseDetailsConverter;
        this.caseMapper = caseMapper;
    }

    private CallbackResponse determineState(CallbackParams callbackParams) {
        String state = ctscEnabled ? ClaimState.JUDGMENT_REQUESTED.getValue() : ClaimState.OPEN.getValue();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        CCDCase updateCCDCase = ccdCase.toBuilder()
            .state(state).build();
        Map<String, Object> map = caseDetailsConverter.convertToMap(updateCCDCase);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(map)
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
