package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeAlreadyRequestedException;
import uk.gov.hmcts.cmc.claimstore.exceptions.MoreTimeRequestedAfterDeadlineException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

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

        if (claimDeadlineService.isPastDeadline(nowInLocalZone(), claim.getResponseDeadline())) {
            throw new MoreTimeRequestedAfterDeadlineException("You must not request more time after deadline");
        }
    }

}
