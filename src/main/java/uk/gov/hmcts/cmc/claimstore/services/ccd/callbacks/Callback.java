package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

public interface Callback {
    CallbackResponse execute(CallbackParams params) throws Exception;
}
