package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;

import java.util.Objects;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class CountyCourtJudgmentRule {

    private ClaimDeadlineService claimDeadlineService;

    @Autowired
    public CountyCourtJudgmentRule(ClaimDeadlineService claimDeadlineService) {
        this.claimDeadlineService = claimDeadlineService;
    }

    public void assertCountyCourtJudgementCanBeRequested(@NotNull Claim claim) {
        Objects.requireNonNull(claim, "claim object can not be null");
        if (isResponseAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("Response for the claim " + claim.getExternalId() + " was submitted");
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + claim.getExternalId() + " was submitted");
        }

        if (!claimDeadlineService.isPastDeadline(nowInLocalZone(), claim.getResponseDeadline())) {
            throw new ForbiddenActionException(
                "County Court Judgment for claim " + claim.getExternalId() + " cannot be requested yet"
            );
        }
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
