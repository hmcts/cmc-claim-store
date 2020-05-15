package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.mapper.CaseMapper;
import uk.gov.hmcts.cmc.claimstore.config.properties.notifications.NotificationsProperties;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.Callback;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackHandler;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackParams;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.CallbackType;
import uk.gov.hmcts.cmc.claimstore.services.notifications.NotificationService;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;

@Service
public class PaperResponseReviewedCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = List.of(CASEWORKER);
    private static final List<CaseEvent> HANDLED_EVENTS = List.of(CaseEvent.REVIEWED_PAPER_RESPONSE);
    private static final String ALREADY_RESPONDED_ERROR =
        "You can’t process this paper request because the defendant already responded to the claim";

    private final Map<CallbackType, Callback> callbacks = Map.of(
        CallbackType.ABOUT_TO_START, this::verifyResponsePossible,
        CallbackType.ABOUT_TO_SUBMIT, this::updateResponseDeadline
    );

    private final CaseDetailsConverter caseDetailsConverter;

    private final Function<CallbackParams, PaperResponseReviewedHandler> handlerCreator;

    @Autowired
    public PaperResponseReviewedCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseMapper caseMapper,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        NotificationService notificationService,
        NotificationsProperties notificationsProperties
    ) {
        this.caseDetailsConverter = caseDetailsConverter;
        this.handlerCreator = callbackParams -> new PaperResponseReviewedHandler(
            caseDetailsConverter,
            caseMapper,
            responseDeadlineCalculator,
            moreTimeRequestRule,
            notificationService,
            notificationsProperties,
            callbackParams);
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return callbacks;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return HANDLED_EVENTS;
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    private AboutToStartOrSubmitCallbackResponse updateResponseDeadline(CallbackParams callbackParams) {
        return handlerCreator.apply(callbackParams).handle();
    }

    private AboutToStartOrSubmitCallbackResponse verifyResponsePossible(CallbackParams callbackParams) {
        Claim claim = toClaimAfterEvent(callbackParams.getRequest());
        var responseBuilder = AboutToStartOrSubmitCallbackResponse.builder();

        if (claim.getResponse().isPresent() || claim.getRespondedAt() != null) {
            responseBuilder.errors(List.of(ALREADY_RESPONDED_ERROR));
        }

        return responseBuilder.build();
    }

    private Claim toClaimAfterEvent(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }
}
