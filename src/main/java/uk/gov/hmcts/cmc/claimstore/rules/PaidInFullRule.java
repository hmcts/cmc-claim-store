package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantLinkException;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
public class PaidInFullRule {

    public void assertPaidInFull(Claim claim, String claimantId) {
        requireNonNull(claim, "claim object can not be null");
        requireNonNull(claimantId, "submitterId object can not be null");
        if (!claim.getSubmitterId().equals(claimantId)) {
            throw new ClaimantLinkException(
                String.format("Claim %s is not linked with claimant %s", claim.getReferenceNumber(), claimantId)
            );
        }
        if (claim.getMoneyReceivedOn().isPresent()) {
            throw new ConflictException(format("Paid in full for claim %s has been already submitted",
                claim.getExternalId()));
        }
    }

}
