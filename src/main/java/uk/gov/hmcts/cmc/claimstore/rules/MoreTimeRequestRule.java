package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MoreTimeRequestRule {

    public void assertMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }

        if (LocalDate.now().isAfter(claim.getResponseDeadline())) {
            throw new MoreTimeRequestedAfterDeadlineException("You must not request more time after deadline");
        }
    }

    public List<String> validateMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        List<String> validationErrors = new ArrayList<>();
        if (claim.isMoreTimeRequested()) {
            validationErrors.add("More time has already been requested");
        }

        if (LocalDate.now().isAfter(claim.getResponseDeadline())) {
            validationErrors.add(
                String.format("Response deadline %s has already passed", claim.getResponseDeadline())
            );
        }
        return validationErrors;
    }
}
