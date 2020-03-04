package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.Map;

@Builder
@Getter
public class CallbackParams {
    private CallbackType type;
    private CallbackRequest request;
    private Map<Params, Object> params;
    private CallbackVersion version;

    public enum Params {
        BEARER_TOKEN
    }
}
