package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.services.ccd.Role;
import uk.gov.hmcts.cmc.claimstore.utils.CaseDetailsConverter;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.COMPLEX_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.JUDGE_REVIEW_ORDER;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REVIEW_COMPLEX_CASE;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.WAITING_TRANSFER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.CITIZEN;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.JUDGE;
import static uk.gov.hmcts.cmc.claimstore.services.ccd.Role.LEGAL_ADVISOR;

@Service
public class AppInsightsCallbackHandler extends CallbackHandler {
    private static final List<Role> ROLES = ImmutableList.of(LEGAL_ADVISOR, JUDGE, CITIZEN);
    private final AppInsights appInsights;
    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public AppInsightsCallbackHandler(AppInsights appInsights, CaseDetailsConverter caseDetailsConverter) {
        this.appInsights = appInsights;
        this.caseDetailsConverter = caseDetailsConverter;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return ImmutableList.of(JUDGE_REVIEW_ORDER, COMPLEX_CASE, WAITING_TRANSFER, REVIEW_COMPLEX_CASE);
    }

    @Override
    public List<Role> getSupportedRoles() {
        return ROLES;
    }

    @Override
    protected Map<CallbackType, Callback> callbacks() {
        return ImmutableMap.of(CallbackType.SUBMITTED, this::raiseAppInsightEvent);
    }

    private CallbackResponse raiseAppInsightEvent(CallbackParams callbackParams) {
        CaseDetails caseDetails = callbackParams.getRequest().getCaseDetails();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);

        Optional.ofNullable(getEvent(callbackParams))
            .ifPresent(appEvent -> appInsights.trackEvent(appEvent, REFERENCE_NUMBER, claim.getReferenceNumber()));
        return SubmittedCallbackResponse.builder().build();
    }

    private AppInsightsEvent getEvent(CallbackParams callbackParams) {
        switch (CaseEvent.fromValue(callbackParams.getRequest().getEventId())) {
            case JUDGE_REVIEW_ORDER:
                return AppInsightsEvent.RETURNED_TO_LA_FROM_JUDGE;
            case COMPLEX_CASE:
                return AppInsightsEvent.COMPLICATED_FOR_ORDER_PILOT;
            case REVIEW_COMPLEX_CASE:
                return AppInsightsEvent.NOT_TOO_COMPLICATED_FOR_LA;
            case WAITING_TRANSFER:
                return AppInsightsEvent.TRANSFERRED_OUT;
            default:
                return null;
        }
    }
}
