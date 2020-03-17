package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CallbackHandler {

    protected abstract Map<CallbackType, Callback> callbacks();

    public abstract List<CaseEvent> handledEvents();

    public abstract List<Role> getSupportedRoles();

    public void register(Map<String, CallbackHandler> handlers) {
        handledEvents().forEach(
            handledEvent -> handlers.put(handledEvent.getValue(), this));
    }

    public CallbackResponse handle(CallbackParams callbackParams) {
        return Optional.ofNullable(callbacks().get(callbackParams.getType()))
            .map(callback -> {
                try {
                    return callback.execute(callbackParams);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .orElseThrow(() -> new CallbackException(
                String.format("Callback for event %s, type %s not implemented",
                    callbackParams.getRequest().getEventId(),
                    callbackParams.getType())));
    }
}
