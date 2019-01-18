package uk.gov.hmcts.cmc.claimstore.events.ccd;

import feign.FeignException;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.claimstore.services.UserService;
import uk.gov.hmcts.cmc.claimstore.stereotypes.LogExecutionTime;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

import static org.springframework.transaction.event.TransactionPhase.BEFORE_COMMIT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.INTERLOCATORY_JUDGEMENT;
import static uk.gov.hmcts.cmc.ccd.domain.CaseEvent.REJECT_ORGANISATION_PAYMENT_PLAN;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.CCD_LINK_DEFENDANT_ID;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.CLAIM_EXTERNAL_ID;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsights.REFERENCE_NUMBER;
import static uk.gov.hmcts.cmc.claimstore.appinsights.AppInsightsEvent.CCD_ASYNC_FAILURE;

@Async("threadPoolTaskExecutor")
public class CCDCaseHandler {
    private final CCDCaseRepository ccdCaseRepository;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;
    private AppInsights appInsights;
    private UserService userService;

    public CCDCaseHandler(
        CCDCaseRepository ccdCaseRepository,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator,
        AppInsights appInsights,
        UserService userService
    ) {
        this.ccdCaseRepository = ccdCaseRepository;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
        this.appInsights = appInsights;
        this.userService = userService;
    }

    @EventListener
    @LogExecutionTime
    public void savePrePayment(CCDPrePaymentEvent event) {
        try {
            ccdCaseRepository.savePrePaymentClaim(event.getExternalId(), event.getAuthorisation());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, CLAIM_EXTERNAL_ID, event.getExternalId());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void saveClaim(CCDClaimIssuedEvent event) {
        Claim claim = event.getClaim();
        try {
            String authorization = event.getAuthorization();

            Long prePaymentClaimId = ccdCaseRepository.getOnHoldIdByExternalId(claim.getExternalId(), authorization);

            Claim ccdClaim = Claim.builder()
                .id(prePaymentClaimId)
                .claimData(claim.getClaimData())
                .submitterId(claim.getSubmitterId())
                .issuedOn(claim.getIssuedOn())
                .responseDeadline(claim.getResponseDeadline())
                .externalId(claim.getExternalId())
                .submitterEmail(claim.getSubmitterEmail())
                .createdAt(claim.getCreatedAt())
                .letterHolderId(claim.getLetterHolderId())
                .features(claim.getFeatures())
                .referenceNumber(claim.getReferenceNumber())
                .build();

            ccdCaseRepository.saveClaim(authorization, ccdClaim);
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void saveDefendantResponse(CCDDefendantResponseEvent event) {
        Claim claim = event.getClaim();
        try {
            Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

            String authorization = event.getAuthorization();
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.saveDefendantResponse(ccdClaim, claim.getDefendantEmail(), response, authorization);

            if (isFullDefenceWithNoMediation(response)) {
                LocalDate deadline = directionsQuestionnaireDeadlineCalculator
                    .calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime.now());
                ccdCaseRepository.updateDirectionsQuestionnaireDeadline(ccdClaim, deadline, authorization);
            }
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    @EventListener
    @LogExecutionTime
    public void requestMoreTimeForResponse(CCDMoreTimeRequestedEvent event) {
        try {
            Claim claim = ccdCaseRepository.getClaimByExternalId(event.getExternalId(), event.getAuthorization())
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.requestMoreTimeForResponse(event.getAuthorization(), claim, event.getNewDeadline());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, CLAIM_EXTERNAL_ID, event.getExternalId());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void saveCountyCourtJudgment(CCDCountyCourtJudgmentEvent event) {
        Claim claim = event.getClaim();
        String authorization = event.getAuthorization();
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.saveCountyCourtJudgment(authorization, ccdClaim, event.getCountyCourtJudgment());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }

    }

    @TransactionalEventListener(phase = BEFORE_COMMIT)
    @LogExecutionTime
    public void saveClaimantResponse(CCDClaimantResponseEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.saveClaimantResponse(ccdClaim, event.getResponse(), authorization);
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    @EventListener
    @LogExecutionTime
    public void linkDefendantToClaim(CCDLinkDefendantEvent event) {
        String authorisation = event.getAuthorisation();
        try {
            ccdCaseRepository.linkDefendant(authorisation);
        } catch (FeignException e) {
            String id = userService.getUserDetails(authorisation).getId();
            appInsights.trackEvent(CCD_ASYNC_FAILURE, CCD_LINK_DEFENDANT_ID, id);
            throw e;
        }
    }

    //    @EventListener
    @LogExecutionTime
    public void linkSealedClaimDocument(CCDLinkSealedClaimDocumentEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.linkSealedClaimDocument(authorization, ccdClaim, event.getSealedClaimDocument());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void updateSettlement(CCDSettlementEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.updateSettlement(ccdClaim, event.getSettlement(), authorization, event.getUserAction());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void savePaidInFull(CCDPaidInFullEvent event) {
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(
                event.getClaim().getExternalId(), event.getAuthorization()
            ).orElseThrow(IllegalStateException::new);

            ccdCaseRepository.paidInFull(ccdClaim, event.getPaidInFull(), event.getAuthorization());
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, event.getClaim().getReferenceNumber());
            throw e;
        }
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void saveInterlocutoryJudgment(CCDInterlocutoryJudgmentEvent event) {
        saveCaseEvent(event.getClaim(), event.getAuthorization(), INTERLOCATORY_JUDGEMENT);
    }

    @TransactionalEventListener
    @LogExecutionTime
    public void saveRejectOrganisationPaymentPlan(CCDRejectOrganisationPaymentPlanEvent event) {
        saveCaseEvent(event.getClaim(), event.getAuthorization(), REJECT_ORGANISATION_PAYMENT_PLAN);
    }

    private void saveCaseEvent(Claim claim, String authorization, CaseEvent caseEvent) {
        try {
            Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
                .orElseThrow(IllegalStateException::new);

            ccdCaseRepository.saveCaseEvent(authorization, ccdClaim, caseEvent);
        } catch (FeignException e) {
            appInsights.trackEvent(CCD_ASYNC_FAILURE, REFERENCE_NUMBER, claim.getReferenceNumber());
            throw e;
        }
    }

    private static boolean isFullDefenceWithNoMediation(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE)
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }
}
