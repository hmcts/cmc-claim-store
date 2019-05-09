package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.rules.MoreTimeRequestRule;
import uk.gov.hmcts.cmc.claimstore.services.ResponseDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.utils.CCDCaseDataToClaim;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse.AboutToStartOrSubmitCallbackResponseBuilder;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;

@Service
public class MoreTimeRequestedCallbackHandler extends CallbackHandler {

    private final EventProducer eventProducer;
    private final AppInsights appInsights;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CCDCaseDataToClaim ccdCaseDataToClaim;

    @Autowired
    public MoreTimeRequestedCallbackHandler(
        EventProducer eventProducer,
        AppInsights appInsights,
        ResponseDeadlineCalculator responseDeadlineCalculator,
        MoreTimeRequestRule moreTimeRequestRule,
        CCDCaseDataToClaim ccdCaseDataToClaim) {
        this.eventProducer = eventProducer;
        this.appInsights = appInsights;
        this.responseDeadlineCalculator = responseDeadlineCalculator;
        this.moreTimeRequestRule = moreTimeRequestRule;
        this.ccdCaseDataToClaim = ccdCaseDataToClaim;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(
            CallbackType.ABOUT_TO_START, params -> this.validateMoreTimeOnPaper(params.getRequest()),
            CallbackType.ABOUT_TO_SUBMIT, params -> requestMoreTimeOnPaper(params.getRequest()),
            CallbackType.SUBMITTED, this::requestMoreTimeOnPaperSubmitted
        );
    }

    @Override
    public CaseEvent handledEvent() {
        return CaseEvent.MORE_TIME_REQUESTED_PAPER;
    }

    private CallbackResponse requestMoreTimeOnPaperSubmitted(CallbackParams callbackParams) {

        Claim claim = convertCallbackToClaim(callbackParams.getRequest());

        eventProducer.createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().orElse(null)
        );
        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED_PAPER, REFERENCE_NUMBER, claim.getReferenceNumber());

        return SubmittedCallbackResponse.builder()
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(
        CallbackRequest callbackRequest
    ) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        List<String> validationResult = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

        if (!validationResult.isEmpty()) {
            return builder
                .errors(validationResult)
                .build();
        }

        LocalDate newDeadline = responseDeadlineCalculator.calculatePostponedResponseDeadline(claim.getIssuedOn());

        Map<String, Object> data = new HashMap<>(callbackRequest.getCaseDetails().getData());
        data.put("moreTimeRequested", CCDYesNoOption.YES);
        data.put("responseDeadline", newDeadline);

        return builder
            .data(data)
            .build();
    }

    private AboutToStartOrSubmitCallbackResponse validateMoreTimeOnPaper(
        CallbackRequest callbackRequest
    ) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        List<String> validationResult = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .errors(validationResult)
            .build();
    }

    @SuppressWarnings("unchecked")
    private Claim convertCallbackToClaim(CallbackRequest callbackRequest) {
        return ccdCaseDataToClaim.to(
            callbackRequest.getCaseDetails().getId(),
            callbackRequest.getCaseDetails().getData()
        );
    }
}
