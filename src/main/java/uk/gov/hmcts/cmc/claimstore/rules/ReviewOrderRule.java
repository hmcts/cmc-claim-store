package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.claimstore.exceptions.ConflictException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Component
public class ReviewOrderRule {

    public void assertReviewOrder(Claim claim) {
        requireNonNull(claim, "claim object can not be null");

        if (claim.getReviewOrder().isPresent()) {
            throw new ConflictException(format("ReviewOrder for the claim %s has been already submitted",
                claim.getExternalId()));
        }
    }

}
