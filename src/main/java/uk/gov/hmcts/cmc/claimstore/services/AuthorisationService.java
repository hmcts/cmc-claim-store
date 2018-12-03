package uk.gov.hmcts.cmc.claimstore.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.lang.String.join;

@Service
public class AuthorisationService {

    public boolean isSubmitterOnClaim(Claim claim, String userId) {
        return userId.equals(claim.getSubmitterId());
    }

    public void assertIsSubmitterOnClaim(Claim claim, String userId) {
        if (!isSubmitterOnClaim(claim, userId)) {
            throw new ForbiddenActionException(
                String.format(
                    "Provided user %s is not a submitter on this claim (%s)",
                    userId,
                    claim.getSubmitterId()
                )
            );
        }
    }

    public boolean isDefendantOnClaim(Claim claim, String userId) {
        return userId.equals(claim.getDefendantId());
    }

    public void assertIsParticipantOnClaim(Claim claim, String userId) {
        if (!isDefendantOnClaim(claim, userId) && !isSubmitterOnClaim(claim, userId)) {
            throw new ForbiddenActionException(
                String.format(
                    "Provided user %s is not a participant on this claim (%s)",
                    userId,
                    join(", ", claim.getSubmitterId(), claim.getDefendantId())
                )
            );
        }
    }

}
