package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.List;
import java.util.Map;

abstract class AbstractStateChangeCallbackHandler extends CallbackHandler {

    private final List<CaseEvent> events;
    private final List<Role> roles;

    private final CaseDetailsConverter caseDetailsConverter;

    AbstractStateChangeCallbackHandler(List<CaseEvent> events,
                                       List<Role> roles,
                                       CaseDetailsConverter caseDetailsConverter) {
        this.events = events;
        this.roles = roles;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    Map<String, Object> updateState(CallbackParams callbackParams, ClaimState state) {

        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        ccdCase.setState(state.getValue());

        return caseDetailsConverter.convertToMap(ccdCase);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return roles;
    }

}
