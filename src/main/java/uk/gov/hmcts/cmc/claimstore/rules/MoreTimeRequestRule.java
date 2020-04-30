package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MoreTimeRequestRule {

    public static final String ALREADY_REQUESTED_MORE_TIME_ERROR = "The defendant already asked for more time "
            + "and their request was processed";
    public static final String ALREADY_RESPONDED_ERROR = "You can't process this request for more "
            + "time because the defendant has responded to the claim.";

    public void assertMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "Claim object can not be null");

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }
    }

    public List<String> validateMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "Claim object can not be null");

        List<String> validationErrors = new ArrayList<>();

        if (claim.getRespondedAt() != null) {
            validationErrors.add(ALREADY_RESPONDED_ERROR);
        }

        if (claim.isMoreTimeRequested()) {
            validationErrors.add(ALREADY_REQUESTED_MORE_TIME_ERROR);
        }
        return validationErrors;
    }

}
