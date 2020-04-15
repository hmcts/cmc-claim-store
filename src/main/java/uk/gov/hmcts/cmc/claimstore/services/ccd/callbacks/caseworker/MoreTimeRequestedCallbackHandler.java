package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class MoreTimeRequestedCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPONSE_MORE_TIME);

    private final EventProducer eventProducer;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CaseDetailsConverter caseDetailsConverter;

    private static final String PREVIEW_SENTENCE = "The response deadline will be %s .";

    @Autowired
    public MoreTimeRequestedCallbackHandler(
        EventProducer eventProducer,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        CaseDetailsConverter caseDetailsConverter) {
        this.eventProducer = eventProducer;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, this::requestMoreTimeViaCaseworker,
            CallbackType.ABOUT_TO_SUBMIT, this::sendNotifications
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    public AboutToStartOrSubmitCallbackResponse sendNotifications(CallbackParams callbackParams) {
        Claim claim = convertCallbackToClaim(callbackParams.getRequest());

        eventProducer.createMoreTimeForResponseRequestedEvent(
                claim,
                claim.getResponseDeadline(),
                claim.getClaimData().getDefendant().getEmail().orElse(null)
        );

        return AboutToStartOrSubmitCallbackResponse
                .builder()
                .build();
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeViaCaseworker(CallbackParams callbackParams) {
        CallbackRequest callbackRequest = callbackParams.getRequest();
        Claim claim = convertCallbackToClaim(callbackRequest);
        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

      //  List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim, newDeadline);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

//        if (!validationResult.isEmpty()) {
//            return builder
//                .errors(validationResult)
//                .build();
//        }

        Map<String, Object> data = new HashMap<>(callbackRequest.getCaseDetails().getData());
        data.put("responseDeadlinePreview", String.format(PREVIEW_SENTENCE, newDeadline));

        return builder
                .data(data)
                .build();
    }

    private Claim convertCallbackToClaim(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }
}
