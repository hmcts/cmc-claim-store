package uk.gov.hmcts.cmc.claimstore.services.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.GenerateOrderCallbackService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.MoreTimeRequestedCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import static java.lang.String.format;

@Component
public class CallbackService {

    private final GenerateOrderCallbackService generateOrderCallbackService;
    private final MoreTimeRequestedCallbackService moreTimeRequestedCallbackService;

    @Autowired
    public CallbackService(
        MoreTimeRequestedCallbackService moreTimeRequestedCallbackService,
        GenerateOrderCallbackService generateOrderCallbackService) {
        this.moreTimeRequestedCallbackService = moreTimeRequestedCallbackService;
        this.generateOrderCallbackService = generateOrderCallbackService;
    }

    public CallbackResponse dispatch(
        String authorisation,
        CallbackType callbackType,
        CallbackRequest callbackRequest) {
        CaseEvent caseEvent = CaseEvent.fromValue(callbackRequest.getEventId());
        switch (caseEvent) {
            case MORE_TIME_REQUESTED_PAPER:
                return moreTimeRequestedCallbackService.execute(callbackType, callbackRequest);
            case GENERATE_ORDER:
                return generateOrderCallbackService.execute(callbackType, callbackRequest, authorisation);
            default:
                throw new CallbackException(format("Unsupported event %s for callbacks",
                    callbackRequest.getEventId()));
        }
    }
}
