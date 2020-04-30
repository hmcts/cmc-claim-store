package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams.Params.BEARER_TOKEN;

@Service
public class CallbackHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final HashMap<String, CallbackHandler> eventHandlers = new HashMap<>();

    @Autowired(required = false)
    public CallbackHandlerFactory(List<CallbackHandler> beans, UserService userService) {
        this.userService = userService;
        beans.forEach(bean -> bean.register(eventHandlers));
    }

    @LogExecutionTime
    public CallbackResponse dispatch(CallbackParams callbackParams) {
        String authorisation = callbackParams.getParams().get(BEARER_TOKEN).toString();
        String eventId = callbackParams.getRequest().getEventId();
        return Optional.ofNullable(eventHandlers.get(eventId))
            .filter(h -> hasSupportedRoles(h, authorisation, eventId))
            .map(h -> h.handle(callbackParams))
            .orElseThrow(() -> new CallbackException("Could not handle callback for event " + eventId));
    }

    private boolean hasSupportedRoles(CallbackHandler callbackHandler, String authorisation, String eventId) {
        List<String> userRoles = userService.getUserDetails(authorisation).getRoles();
        logger.info("Idam provided roles {}", String.join(",", userRoles));
        logger.info("CCD provided supported roles {}", callbackHandler.getSupportedRoles().stream()
            .map(Role::getRole)
            .collect(Collectors.joining(",")));

        if (callbackHandler.getSupportedRoles().stream().anyMatch(role -> userRoles.contains(role.getRole()))) {
            return true;
        } else {
            throw new ForbiddenActionException("User does not have supported role for event " + eventId);
        }
    }
}
