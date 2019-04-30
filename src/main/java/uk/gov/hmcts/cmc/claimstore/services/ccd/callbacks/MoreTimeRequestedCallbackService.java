package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;
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

import static java.lang.String.format;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_MORE_TIME_REQUESTED_PAPER;

@Service
public class MoreTimeRequestedCallbackService {

    private final EventProducer eventProducer;
    private final AppInsights appInsights;
    private final ResponseDeadlineCalculator responseDeadlineCalculator;
    private final MoreTimeRequestRule moreTimeRequestRule;
    private final CCDCaseDataToClaim ccdCaseDataToClaim;

    @Autowired
    public MoreTimeRequestedCallbackService(
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

    public CallbackResponse execute(CallbackType callbackType, CallbackRequest callbackRequest) {
        switch (callbackType) {
            case ABOUT_TO_START:
                return requestMoreTimeOnPaper(callbackRequest, true);
            case ABOUT_TO_SUBMIT:
                return requestMoreTimeOnPaper(callbackRequest, false);
            case SUBMITTED:
                return requestMoreTimeOnPaperSubmitted(callbackRequest);
            default:
                throw new IllegalArgumentException(
                    format("Callback for event %s, type %s not implemented",
                        callbackRequest.getEventId(),
                        callbackType));
        }
    }

    private AboutToStartOrSubmitCallbackResponse requestMoreTimeOnPaper(
        CallbackRequest callbackRequest,
        boolean validateOnly
    ) {
        Claim claim = convertCallbackToClaim(callbackRequest);

        List<String> validationResult = moreTimeRequestRule.validateMoreTimeCanBeRequested(claim);
        AboutToStartOrSubmitCallbackResponseBuilder builder = AboutToStartOrSubmitCallbackResponse
            .builder();

        if (validateOnly || !validationResult.isEmpty()) {
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

    private SubmittedCallbackResponse requestMoreTimeOnPaperSubmitted(
        CallbackRequest callbackRequest) {

        Claim claim = convertCallbackToClaim(callbackRequest);

        eventProducer.createMoreTimeForResponseRequestedEvent(
            claim,
            claim.getResponseDeadline(),
            claim.getClaimData().getDefendant().getEmail().orElse(null)
        );
        appInsights.trackEvent(RESPONSE_MORE_TIME_REQUESTED_PAPER, REFERENCE_NUMBER, claim.getReferenceNumber());

        return SubmittedCallbackResponse.builder()
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
