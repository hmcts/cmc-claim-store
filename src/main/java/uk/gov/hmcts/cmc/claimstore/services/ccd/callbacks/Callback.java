package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.net.URISyntaxException;

public interface Callback {
    CallbackResponse execute(CallbackParams params) throws URISyntaxException;
}
