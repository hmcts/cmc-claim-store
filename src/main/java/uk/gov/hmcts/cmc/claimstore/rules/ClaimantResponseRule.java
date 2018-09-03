package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantResponseAlreadySubmittedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;

@Service
public class ClaimantResponseRule {

    public void assertCanBeRequested(Claim claim, String claimantId) {
        if (!isClaimLinkedWithClaimant(claim, claimantId)) {
            throw new ClaimantLinkException(
                String.format("Claim %s is not linked with claimant %s", claim.getReferenceNumber(), claimantId)
            );
        }

        if (isDefendantResponseNotSubmitted(claim)) {
            throw new ForbiddenActionException(
                String.format(
                    "Defendant response for the claim %s has not been submitted, but claimant response requested",
                    claim.getExternalId()
                )
            );
        }

        if (isClaimantResponseAlreadySubmitted(claim)) {
            throw new ClaimantResponseAlreadySubmittedException(claim.getExternalId());
        }
    }

    private boolean isDefendantResponseNotSubmitted(Claim claim) {
        return claim.getRespondedAt() == null;
    }

    private boolean isClaimLinkedWithClaimant(Claim claim, String claimantId) {
        return claim.getSubmitterId() != null && claim.getSubmitterId().equals(claimantId);
    }

    private boolean isClaimantResponseAlreadySubmitted(Claim claim) {
        return claim.getClaimantRespondedAt().isPresent();
    }
}
