package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ForbiddenActionException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.response.ResponseType;

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

    public void assertCountyCourtJudgementCanBeRequested(@NotNull Claim claim, boolean isByAdmission) {
        Objects.requireNonNull(claim, "claim object can not be null");
        String externalId = claim.getExternalId();
        if (!isByAdmission) {
            if (isResponseAlreadySubmitted(claim)) {
                throw new ForbiddenActionException("Response for the claim " + externalId + " was submitted");
            }

            if (!claimDeadlineService.isPastDeadline(nowInLocalZone(), claim.getResponseDeadline())) {
                throw new ForbiddenActionException(
                    "County Court Judgment for claim " + externalId + " cannot be requested yet"
                );
            }
        } else {
            Response response = claim.getResponse().orElseThrow(IllegalArgumentException::new);
            if (!isAnAdmissionResponse(response)) {
                throw new ForbiddenActionException("County Court Judgment for claim "
                    + externalId
                    + " cannot be issued for "
                    + response.getResponseType().name()
                );
            }
        }

        if (isCountyCourtJudgmentAlreadySubmitted(claim)) {
            throw new ForbiddenActionException("County Court Judgment for the claim "
                + externalId + " was submitted");
        }
    }

    private boolean isAnAdmissionResponse(Response response) {
        return response.getResponseType().equals(ResponseType.PART_ADMISSION)
            || response.getResponseType().equals(ResponseType.FULL_ADMISSION);
    }

    private boolean isResponseAlreadySubmitted(Claim claim) {
        return null != claim.getRespondedAt();
    }

    private boolean isCountyCourtJudgmentAlreadySubmitted(Claim claim) {
        return claim.getCountyCourtJudgment() != null;
    }
}
