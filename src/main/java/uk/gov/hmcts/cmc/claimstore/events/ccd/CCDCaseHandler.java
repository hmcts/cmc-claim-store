package uk.gov.hmcts.cmc.claimstore.events.ccd;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import uk.gov.hmcts.cmc.claimstore.repositories.CCDCaseRepository;
import uk.gov.hmcts.cmc.claimstore.services.DirectionsQuestionnaireDeadlineCalculator;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Predicate;

public class CCDCaseHandler {
    private final CCDCaseRepository ccdCaseRepository;
    private final DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator;

    public CCDCaseHandler(
        CCDCaseRepository ccdCaseRepository,
        DirectionsQuestionnaireDeadlineCalculator directionsQuestionnaireDeadlineCalculator
    ) {
        this.ccdCaseRepository = ccdCaseRepository;
        this.directionsQuestionnaireDeadlineCalculator = directionsQuestionnaireDeadlineCalculator;
    }

    @EventListener
    @Async
    public void savePrePayment(CCDPrePaymentEvent event) {
        ccdCaseRepository.savePrePaymentClaim(event.getExternalId(), event.getAuthorisation());
    }

    @EventListener
    @Async
    public void saveClaim(CCDClaimIssuedEvent event) {
        Claim claim = event.getClaim();
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
    }

    @EventListener
    @Async
    public void saveDefendantResponse(CCDDefendantResponseEvent event) {
        Claim claim = event.getClaim();
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        String authorization = event.getAuthorization();
        Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
            .orElseThrow(IllegalStateException::new);

        ccdCaseRepository.saveDefendantResponse(ccdClaim, claim.getDefendantEmail(), response, authorization);
        if (isFullDefenceWithNoMediation(response)) {
            LocalDate deadline = directionsQuestionnaireDeadlineCalculator
                .calculateDirectionsQuestionnaireDeadlineCalculator(LocalDateTime.now());
            ccdCaseRepository.updateDirectionsQuestionnaireDeadline(claim, deadline, authorization);
        }
    }

    @EventListener
    @Async
    public void requestMoreTimeForResponse(CCDMoreTimeRequestedEvent event) {
        Claim claim = ccdCaseRepository.getClaimByExternalId(event.getExternalId(), event.getAuthorization())
            .orElseThrow(IllegalStateException::new);

        ccdCaseRepository.requestMoreTimeForResponse(event.getAuthorization(), claim, event.getNewDeadline());
    }

    @EventListener
    @Async
    public void saveCountyCourtJudgment(CCDCountyCourtJudgmentEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
            .orElseThrow(IllegalStateException::new);

        ccdCaseRepository.saveCountyCourtJudgment(authorization, ccdClaim, event.getCountyCourtJudgment(),
            event.isIssue());

    }

    @EventListener
    @Async
    public void saveClaimantResponse(CCDClaimantResponseEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
            .orElseThrow(IllegalStateException::new);

        ccdCaseRepository.saveClaimantResponse(ccdClaim, event.getResponse(), authorization);
    }

    @EventListener
    @Async
    public void linkDefendantToClaim(CCDLinkDefendantEvent event) {
        ccdCaseRepository.linkDefendant(event.getAuthorisation());
    }

    @EventListener
    @Async
    public void linkSealedClaimDocument(CCDLinkSealedClaimDocumentEvent event) {
        String authorization = event.getAuthorization();
        Claim claim = event.getClaim();
        Claim ccdClaim = ccdCaseRepository.getClaimByExternalId(claim.getExternalId(), authorization)
            .orElseThrow(IllegalStateException::new);

        ccdCaseRepository.linkSealedClaimDocument(authorization, ccdClaim, event.getSealedClaimDocument());
    }

    private static boolean isFullDefenceWithNoMediation(Response response) {
        return response.getResponseType().equals(ResponseType.FULL_DEFENCE)
            && response.getFreeMediation().filter(Predicate.isEqual(YesNoOption.NO)).isPresent();
    }
}
