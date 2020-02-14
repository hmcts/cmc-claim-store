package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class MoreTimeRequestRule {

    public static final String ALREADY_REQUESTED_MORE_TIME_ERROR = "The defendant already asked for more time "
            + "and their request was processed";
    public static final String PAST_DEADLINE_ERROR = "The defendant has missed the deadline for requesting more time";
    public static final String ALREADY_RESPONDED_ERROR = "You can’t process this paper request for more time "
            + "because the defendant already responded to the claim digitally.";

    private final ClaimDeadlineService claimDeadlineService;

    @Autowired
    public MoreTimeRequestRule(ClaimDeadlineService claimDeadlineService) {
        this.claimDeadlineService = claimDeadlineService;
    }

    public void assertMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }
    }

    public List<String> validateMoreTimeCanBeRequested(Claim claim, LocalDate newDeadline) {
        Objects.requireNonNull(claim, "claim object can not be null");

        List<String> validationErrors = new ArrayList<>();

        if (claim.getRespondedAt() != null) {
            validationErrors.add(ALREADY_RESPONDED_ERROR);
        }

        if (claim.isMoreTimeRequested()) {
            validationErrors.add(ALREADY_REQUESTED_MORE_TIME_ERROR);
        }

        if (claimDeadlineService.isPastDeadline(nowInLocalZone(), newDeadline)) {
            validationErrors.add(PAST_DEADLINE_ERROR);
        }

        return validationErrors;
    }

}
