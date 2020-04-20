package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDContactPartyType;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.ccd.domain.GeneralLetterContent;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.events.response.MoreTimeRequestedCitizenNotificationHandler;
import uk.gov.hmcts.cmc.claimstore.idam.models.UserDetails;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.RESPONSE_MORE_TIME;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CASEWORKER;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

@Service
@ConditionalOnProperty("feature_toggles.ctsc_enabled")
public class MoreTimeRequestedCallbackHandler extends CallbackHandler {

    private static final List<Role> ROLES = Collections.singletonList(CASEWORKER);

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESPONSE_MORE_TIME);

    private final EventProducer eventProducer;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CaseDetailsConverter caseDetailsConverter;
    private final MoreTimeRequestedCitizenNotificationHandler moreTimeRequestedCitizenNotificationHandler;

    private static final String PREVIEW_SENTENCE = "The response deadline will be %s .";

    @Autowired
    public MoreTimeRequestedCallbackHandler(
            EventProducer eventProducer,
            ResponseDeadlineCalculator responseDeadlineCalculator,
            MoreTimeRequestRule moreTimeRequestRule,
            CaseDetailsConverter caseDetailsConverter,
            MoreTimeRequestedCitizenNotificationHandler moreTimeRequestedCitizenNotificationHandler) {
        this.eventProducer = eventProducer;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.caseDetailsConverter = caseDetailsConverter;
        this.moreTimeRequestedCitizenNotificationHandler = moreTimeRequestedCitizenNotificationHandler;
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

    public CallbackResponse sendNotifications(CallbackParams callbackParams) {
        Claim claim = convertCallbackToClaim(callbackParams.getRequest());

        eventProducer.createMoreTimeForResponseRequestedEvent(
                claim,
                claim.getResponseDeadline(),
                claim.getClaimData().getDefendant().getEmail().orElse(null)
        );

        return moreTimeRequestedCitizenNotificationHandler.sendNotifications(callbackParams);
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeViaCaseworker(CallbackParams callbackParams) {
        System.out.println("REQUEST MORE TIME VIA CASEWORKER");
        CallbackRequest callbackRequest = callbackParams.getRequest();

        Claim claim = convertCallbackToClaim(callbackRequest);
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(callbackRequest.getCaseDetails());

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        CCDRespondent respondent = ccdCase.getRespondents().get(0).getValue().toBuilder()
                .responseDeadline(newDeadline)
                .build();

        CCDCase updatedCCDCase = ccdCase.toBuilder()
                .respondents(List.of(CCDCollectionElement.<CCDRespondent>builder()
                        .value(respondent)
                        .build()))
                .build();

        List<String> validationResult = this.moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

        if (!validationResult.isEmpty()) {
            return builder
                .errors(validationResult)
                .build();
        }

        Map<String, Object> data = new HashMap<>(caseDetailsConverter.convertToMap(updatedCCDCase));
        data.put("responseDeadlinePreview", String.format(PREVIEW_SENTENCE, formatDate(newDeadline)));

        return builder
                .data(data)
                .build();
    }

    private Claim convertCallbackToClaim(CallbackRequest callbackRequest) {
        return caseDetailsConverter.extractClaim(callbackRequest.getCaseDetails());
    }
}
