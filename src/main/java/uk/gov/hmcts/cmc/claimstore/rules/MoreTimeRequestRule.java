package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MoreTimeRequestRule {

    public static final String ALREADY_REQUESTED_MORE_TIME_ERROR =
        "The defendant already asked for more time and their request was processed";
    public static final String PAST_DEADLINE_ERROR = "The defendant has missed the deadline for requesting more time";

    public void assertMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }

        assertIsNotPastDeadline(LocalDateTimeFactory.nowInLocalZone(), claim.getResponseDeadline());
    }

    void assertIsNotPastDeadline(LocalDateTime now, LocalDate responseDeadline) {
        if (isPastDeadline(now, responseDeadline)) {
            throw new MoreTimeRequestedAfterDeadlineException("You must not request more time after deadline");
        }
    }

    public List<String> validateMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        List<String> validationErrors = new ArrayList<>();
        if (claim.isMoreTimeRequested()) {
            validationErrors.add(ALREADY_REQUESTED_MORE_TIME_ERROR);
        }

        if (isPastDeadline(LocalDateTimeFactory.nowInLocalZone(), claim.getResponseDeadline())) {
            validationErrors.add(PAST_DEADLINE_ERROR);
        }
        return validationErrors;
    }

    private boolean isPastDeadline(LocalDateTime now, LocalDate responseDeadline) {
        LocalDateTime responseDeadlineTime = responseDeadline.atTime(16, 0);
        return now.isEqual(responseDeadlineTime) || now.isAfter(responseDeadlineTime);
    }
}
