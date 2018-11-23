package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.CCDEventProducer;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED;

@Service
public class DefendantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;
    private final AppInsights appInsights;
    private CCDEventProducer ccdEventProducer;

    public DefendantResponseService(
        EventProducer eventProducer,
        ClaimService claimService,
        UserService userService,
        AppInsights appInsights,
        CCDEventProducer ccdEventProducer
    ) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
        this.appInsights = appInsights;
        this.ccdEventProducer = ccdEventProducer;
    }

    @Transactional(transactionManager = "transactionManager")
    public Claim save(
        String externalId,
        String defendantId,
        Response response,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);

        if (!isClaimLinkedWithDefendant(claim, defendantId)) {
            throw new DefendantLinkingException(
                String.format("Claim %s is not linked with defendant %s", claim.getReferenceNumber(), defendantId)
            );
        }

        if (isResponseAlreadySubmitted(claim)) {
            throw new ResponseAlreadySubmittedException(claim.getId());
        }

        if (isCCJAlreadyRequested(claim)) {
            throw new CountyCourtJudgmentAlreadyRequestedException(claim.getId());
        }

        String defendantEmail = userService.getUserDetails(authorization).getEmail();
        claimService.saveDefendantResponse(claim, defendantEmail, response, authorization);

        Claim claimAfterSavingResponse = claimService.getClaimByExternalId(externalId, authorization);

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse);
        ccdEventProducer.createCCDDefendantResponseEvent(claimAfterSavingResponse, authorization);

        appInsights.trackEvent(getAppInsightsEventName(response.getResponseType()), claim.getReferenceNumber());

        return claimAfterSavingResponse;
    }

    public AppInsightsEvent getAppInsightsEventName(ResponseType responseType) {
        requireNonNull(responseType, "responseType must not be null");
        switch (responseType) {
            case FULL_ADMISSION:
                return RESPONSE_FULL_ADMISSION_SUBMITTED;
            case PART_ADMISSION:
                return RESPONSE_PART_ADMISSION_SUBMITTED;
            case FULL_DEFENCE:
                return RESPONSE_FULL_DEFENCE_SUBMITTED;
            default:
                throw new IllegalArgumentException("Invalid response type " + responseType);
        }
    }

    private boolean isClaimLinkedWithDefendant(Claim claim, String defendantId) {
        return claim.getDefendantId() != null && claim.getDefendantId().equals(defendantId);
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return claim.getRespondedAt() != null;
    }

    private boolean isCCJAlreadyRequested(Claim claim) {
        return claim.getCountyCourtJudgmentRequestedAt() != null;
    }

}
