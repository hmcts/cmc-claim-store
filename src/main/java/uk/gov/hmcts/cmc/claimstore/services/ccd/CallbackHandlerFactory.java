package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.UserRolesService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@Service
public class CallbackHandlerFactory {

    private final UserRolesService userRolesService;
    private HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();

    @Autowired(required = false)
    public CallbackHandlerFactory(List<CallbackHandler> beans, UserRolesService userRolesService) {
        this.userRolesService = userRolesService;
        beans.forEach(bean -> bean.register(eventHandlers));
    }

    public CallbackResponse dispatch(CallbackParams callbackParams) {
        return Optional.ofNullable(eventHandlers.get(callbackParams.getRequest().getEventId()))
            .filter(h -> hasSupportedRoles(h, callbackParams.getParams().get(BEARER_TOKEN).toString()))
            .map(h -> h.handle(callbackParams))
            .orElseThrow(() -> new CallbackException(
                "Could not handle callback for event " + callbackParams.getRequest().getEventId()));
    }

    private boolean hasSupportedRoles(CallbackHandler callbackHandler, String authorisation) {
        List<String> userRoles = userRolesService.retrieveUserRoles(authorisation);
        return callbackHandler.getSupportedRoles().stream().anyMatch(userRoles::contains);
    }
}
