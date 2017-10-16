package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.claimstore.models.Claim;

@Service
public class AuthorisationService {

    public boolean isPartyOnClaim(Claim claim, Long userId) {
        return userId.equals(claim.getSubmitterId()) || userId.equals(claim.getDefendantId());
    }

    public void assertIsPartyOnClaim(Claim claim, Long userId) {
        if (!isPartyOnClaim(claim, userId)) {
            throw new ForbiddenActionException("Provided user is not a part on this claim");
        }
    }

}
