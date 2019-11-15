package uk.gov.hmcts.cmc.claimstore.events.claim;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.ccd.domain.CaseEvent;
import uk.gov.hmcts.cmc.claimstore.repositories.CaseRepository;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators;
import uk.gov.hmcts.cmc.domain.models.ClaimSubmissionOperationIndicators.ClaimSubmissionOperationIndicatorsBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

@Component
public class ClaimCreationEventsStatusService {

    private final CaseRepository caseRepository;

    @Autowired
    public ClaimCreationEventsStatusService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    public Claim updateClaimOperationCompletion(
        String authorisation,
        Claim claim,
        CaseEvent caseEvent) {

        return caseRepository.updateClaimSubmissionOperationStatus(
            authorisation,
            claim.getId(),
            updateClaimSubmissionIndicatorWithEvent(claim.getClaimSubmissionOperationIndicators(), caseEvent),
            caseEvent);
    }

    public Claim updateClaimOperationCompletion(
        String authorisation,
        Long claimId,
        ClaimSubmissionOperationIndicators claimSubmissionOperationIndicators,
        CaseEvent caseEvent
    ) {
        return caseRepository.updateClaimSubmissionOperationStatus(
            authorisation,
            claimId,
            claimSubmissionOperationIndicators,
            caseEvent);
    }

    private ClaimSubmissionOperationIndicators updateClaimSubmissionIndicatorWithEvent(
        ClaimSubmissionOperationIndicators indicator,
        CaseEvent caseEvent
    ) {
        ClaimSubmissionOperationIndicatorsBuilder updatedIndicator = indicator.toBuilder();

        switch (caseEvent) {
            case PIN_GENERATION_OPERATIONS:
                updatedIndicator.defendantNotification(YesNoOption.YES)
                    .bulkPrint(YesNoOption.YES)
                    .staffNotification(YesNoOption.YES);
                break;
            case CLAIM_ISSUE_RECEIPT_UPLOAD:
                updatedIndicator.claimIssueReceiptUpload(YesNoOption.YES);
                break;
            case SEALED_CLAIM_UPLOAD:
                updatedIndicator.sealedClaimUpload(YesNoOption.YES);
                break;
            case SENDING_RPA:
                updatedIndicator.rpa(YesNoOption.YES);
                break;
            case SENDING_CLAIMANT_NOTIFICATION:
                updatedIndicator.claimantNotification(YesNoOption.YES);
                break;
            default:
                throw new IllegalArgumentException("Unknown case event provided in "
                    + "updateClaimSubmissionIndicatorWithEvent method");
        }
        return updatedIndicator.build();
    }
}
