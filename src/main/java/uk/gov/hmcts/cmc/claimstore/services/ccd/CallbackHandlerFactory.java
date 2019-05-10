package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.DrawOrderCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.GenerateOrderCallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.MoreTimeRequestedCallbackHandler;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.ACTION_REVIEW_COMMENTS;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.DRAW_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_PAPER;

@Service
public class CallbackHandlerFactory {

    private Map<String, CallbackHandler> handlers = new HashMap<>();

    @Autowired
    public CallbackHandlerFactory(
        MoreTimeRequestedCallbackHandler moreTimeRequestedCallbackHandler,
        Optional<GenerateOrderCallbackHandler> generateOrderCallbackHandler,
        DrawOrderCallbackHandler drawOrderCallbackHandler) {
        handlers.put(MORE_TIME_REQUESTED_PAPER.getValue(), moreTimeRequestedCallbackHandler);
        handlers.put(DRAW_ORDER.getValue(), drawOrderCallbackHandler);
        generateOrderCallbackHandler.ifPresent(h -> handlers.put(GENERATE_ORDER.getValue(), h));
        generateOrderCallbackHandler.ifPresent(h -> handlers.put(ACTION_REVIEW_COMMENTS.getValue(), h));
    }

    public CallbackResponse dispatch(CallbackParams callbackParams) {
        return Optional.ofNullable(handlers.get(callbackParams.getRequest().getEventId()))
            .map(h -> h.handle(callbackParams))
            .orElseThrow(() -> new CallbackException("Could not handle callback"));
    }
}
