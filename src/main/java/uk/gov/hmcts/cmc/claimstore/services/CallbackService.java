package uk.gov.hmcts.cmc.claimstore.services;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.domain.exceptions.BadRequestException;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.GENERATE_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.MORE_TIME_REQUESTED_PAPER;

@Service
public class CallbackService {
    public static final String ABOUT_TO_START_CALLBACK = "about-to-start";
    public static final String ABOUT_TO_SUBMIT_CALLBACK = "about-to-submit";
    public static final String SUBMITTED_CALLBACK = "submitted";

    private Map<CaseEvent, Map<String, Callback>> callbacks = ImmutableMap.of(
        MORE_TIME_REQUESTED_PAPER, ImmutableMap.of(
            ABOUT_TO_START_CALLBACK, ClaimService::requestMoreTimeOnPaperValidateOnly,
            ABOUT_TO_SUBMIT_CALLBACK, ClaimService::requestMoreTimeOnPaper,
            SUBMITTED_CALLBACK, ClaimService::requestMoreTimeOnPaperSubmitted
        ),
        GENERATE_ORDER, ImmutableMap.of(
            ABOUT_TO_START_CALLBACK, ClaimService::prepopulateFields
        )
    );

    public Callback getCallbackFor(String eventId, String callbackType) {
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        
        return Optional.ofNullable(callbacks.get(caseEvent))
            .map(callbacks -> callbacks.get(callbackType))
            .orElseThrow(() -> new BadRequestException(
                format("Callback for event %s, type %s not implemented", eventId, callbackType)));
    }

    public interface Callback {
        CallbackResponse execute(ClaimService claimService, CallbackRequest callbackRequest);
    }
}
