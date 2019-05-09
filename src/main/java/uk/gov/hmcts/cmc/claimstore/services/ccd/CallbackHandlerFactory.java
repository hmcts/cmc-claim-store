package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class CallbackHandlerFactory {

    private HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();

    @Autowired(required = false)
    public CallbackHandlerFactory(List<CallbackHandler> beans) {
        beans.forEach(bean -> bean.register(eventHandlers));
    }

    public CallbackResponse dispatch(CallbackParams callbackParams) {
        return Optional.ofNullable(eventHandlers.get(callbackParams.getRequest().getEventId()))
            .map(h -> h.handle(callbackParams))
            .orElseThrow(() -> new CallbackException(
                "Could not handle callback for event " + callbackParams.getRequest().getEventId()));
    }
}
