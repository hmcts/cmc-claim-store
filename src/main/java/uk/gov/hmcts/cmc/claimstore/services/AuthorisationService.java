package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

@Service
public class AuthorisationService {

    public boolean isSubmitterOnClaim(Claim claim, Long userId) {
        return userId.equals(claim.getSubmitterId());
    }

    public void assertIsSubmitterOnClaim(Claim claim, Long userId) {
        if (!isSubmitterOnClaim(claim, userId)) {
            throw new ForbiddenActionException("Provided user is not a submitter on this claim");
        }
    }

    public boolean isDefendantOnClaim(Claim claim, Long userId) {
        return userId.equals(claim.getDefendantId());
    }

    public void assertIsDefendantOnClaim(Claim claim, Long userId) {
        if (!isDefendantOnClaim(claim, userId)) {
            throw new ForbiddenActionException("Provided user is not a defendant on this claim");
        }
    }

}
