package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.ClaimState;

import java.util.Map;

abstract class AbstractStateChangeCallbackHandler extends CallbackHandler {

    protected abstract CaseDetailsConverter getCaseDetailsConverter();

    Map<String, Object> updateState(CallbackParams callbackParams, ClaimState state) {

        CaseDetailsConverter caseDetailsConverter = getCaseDetailsConverter();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackParams.getRequest().getCaseDetails());
        ccdCase.setState(state.getValue());

        return caseDetailsConverter.convertToMap(ccdCase);
    }

}
