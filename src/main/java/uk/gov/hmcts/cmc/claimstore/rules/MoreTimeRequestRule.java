package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MoreTimeRequestRule {

    private ClaimDeadlineService claimDeadlineService;

    @Autowired
    public MoreTimeRequestRule(ClaimDeadlineService claimDeadlineService) {
        this.claimDeadlineService = claimDeadlineService;
    }

    public void assertMoreTimeCanBeRequested(Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");

        if (claim.isMoreTimeRequested()) {
            throw new MoreTimeAlreadyRequestedException("You have already requested more time");
        }

        assertIsNotPastDeadline(LocalDateTimeFactory.nowInLocalZone(), claim.getResponseDeadline());
    }

    protected void assertIsNotPastDeadline(LocalDateTime now, LocalDate responseDeadline) {
        if (claimDeadlineService.isPastDeadline(now, responseDeadline)) {
            throw new MoreTimeRequestedAfterDeadlineException("You must not request more time after deadline");
        }
    }

}
