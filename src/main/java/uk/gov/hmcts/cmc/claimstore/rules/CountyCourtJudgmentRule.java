package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.CountyCourtJudgmentType;

import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.utils.LocalDateTimeFactory.nowInLocalZone;

@Service
public class CountyCourtJudgmentRule {

    private ClaimDeadlineService claimDeadlineService;

    @Autowired
    public CountyCourtJudgmentRule(ClaimDeadlineService claimDeadlineService) {
        this.claimDeadlineService = claimDeadlineService;
    }

    public void assertCountyCourtJudgementCanBeRequested(@NotNull Claim claim,
                                                         CountyCourtJudgmentType countyCourtJudgmentType) {
        requireNonNull(claim, "claim object can not be null");
        String externalId = claim.getExternalId();

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + externalId + " was submitted");
        }

        switch (countyCourtJudgmentType) {
            case DEFAULT:
                if (isResponseAlreadySubmitted(claim)) {
                    throw new ForbiddenActionException("Response for the claim " + externalId + " was submitted");
                }

                if (!claimDeadlineService.isPastDeadline(nowInLocalZone(), claim.getResponseDeadline())) {
                    throw new ForbiddenActionException(
                        "County Court Judgment for claim " + externalId + " cannot be requested yet"
                    );
                }
                break;
            case ADMISSIONS:
                if (!claim.getResponse().isPresent()) {
                    throw new IllegalStateException("Claim response cannot be null for judgment type: "
                        + countyCourtJudgmentType);
                }
                break;
            case DETERMINATION:
                // Action pending
                break;
            default:
                throw new ForbiddenActionException("County Court Judgment for claim "
                    + externalId + " is not supported");

        }
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }

    private boolean isCountyCourtJudgmentAlreadyRedetermined(Claim claim) {
        return claim.getReDeterminationRequestedAt().isPresent();
    }

    public void assertRedeterminationCanBeRequestedOnCountyCourtJudgement(Claim claim) {
        requireNonNull(claim, "claim object can not be null");

        String externalId = claim.getExternalId();

        if (!isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + externalId + " is not yet submitted");
        }

        if (isCountyCourtJudgmentAlreadyRedetermined(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + externalId + " has been already redetermined");
        }

    }
}
