package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.exceptions.CountyCourtJudgmentAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.DefendantLinkingException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.utils.FeaturesUtils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DEFENDANT_OPTED_OUT_FOR_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.DEFENDANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_DEFENCE_SUBMITTED;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_FULL_DEFENCE_SUBMITTED_STATES_PAID;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.RESPONSE_PART_ADMISSION_SUBMITTED_STATES_PAID;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.hasDefendantOptedForMediation;
import static uk.gov.hmcts.cmc.domain.utils.ResponseUtils.isResponseStatesPaid;

@Service
public class DefendantResponseService {

    private final EventProducer eventProducer;
    private final ClaimService claimService;
    private final UserService userService;
    private final AppInsights appInsights;

    public DefendantResponseService(
        EventProducer eventProducer,
        ClaimService claimService,
        UserService userService,
        AppInsights appInsights
    ) {
        this.eventProducer = eventProducer;
        this.claimService = claimService;
        this.userService = userService;
        this.appInsights = appInsights;
    }

    public Claim save(
        String externalId,
        String defendantId,
        Response response,
        String authorization
    ) {
        Claim claim = claimService.getClaimByExternalId(externalId, authorization);

        String referenceNumber = claim.getReferenceNumber();
        if (!isClaimLinkedWithDefendant(claim, defendantId)) {
            throw new DefendantLinkingException(
                String.format("Claim %s is not linked with defendant %s", referenceNumber, defendantId)
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

        eventProducer.createDefendantResponseEvent(claimAfterSavingResponse, authorization);

        appInsights.trackEvent(getAppInsightsEventName(response), REFERENCE_NUMBER, referenceNumber);

        if (!hasDefendantOptedForMediation(response)) {
            appInsights.trackEvent(getAppInsightEventForMediation(claim), REFERENCE_NUMBER, referenceNumber);
        }

        return claimAfterSavingResponse;
    }

    public AppInsightsEvent getAppInsightsEventName(Response response) {
        requireNonNull(response, "response must not be null");

        PaymentOption paymentOption;
        ResponseType responseType = response.getResponseType();

        switch (responseType) {
            case FULL_ADMISSION:
                paymentOption = ((FullAdmissionResponse) response).getPaymentIntention().getPaymentOption();
                switch (paymentOption) {
                    case IMMEDIATELY:
                        return RESPONSE_FULL_ADMISSION_SUBMITTED_IMMEDIATELY;
                    case BY_SPECIFIED_DATE:
                        return RESPONSE_FULL_ADMISSION_SUBMITTED_SET_DATE;
                    case INSTALMENTS:
                        return RESPONSE_FULL_ADMISSION_SUBMITTED_INSTALMENTS;
                    default:
                        throw new IllegalArgumentException("Invalid full admission payment option");
                }
            case PART_ADMISSION:
                if (isResponseStatesPaid(response)) {
                    return RESPONSE_PART_ADMISSION_SUBMITTED_STATES_PAID;
                }

                paymentOption = ((PartAdmissionResponse) response).getPaymentIntention()
                    .orElseThrow(IllegalStateException::new)
                    .getPaymentOption();
                switch (paymentOption) {
                    case IMMEDIATELY:
                        return RESPONSE_PART_ADMISSION_SUBMITTED_IMMEDIATELY;
                    case BY_SPECIFIED_DATE:
                        return RESPONSE_PART_ADMISSION_SUBMITTED_SET_DATE;
                    case INSTALMENTS:
                        return RESPONSE_PART_ADMISSION_SUBMITTED_INSTALMENTS;
                    default:
                        throw new IllegalArgumentException("Invalid part admission payment option");
                }

            case FULL_DEFENCE:
                if (isResponseStatesPaid(response)) {
                    return RESPONSE_FULL_DEFENCE_SUBMITTED_STATES_PAID;
                }
                return RESPONSE_FULL_DEFENCE_SUBMITTED;
            default:
                throw new IllegalArgumentException("Invalid response type " + responseType);
        }
    }

    private AppInsightsEvent getAppInsightEventForMediation(Claim claim) {
        return FeaturesUtils.hasMediationPilotFeature(claim)
            ? DEFENDANT_OPTED_OUT_FOR_MEDIATION_PILOT
            : DEFENDANT_OPTED_OUT_FOR_NON_MEDIATION_PILOT;
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
