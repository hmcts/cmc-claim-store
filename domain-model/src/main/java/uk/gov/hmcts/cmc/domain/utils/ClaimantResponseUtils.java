package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.FormaliseOption;
import uk.gov.hmcts.cmc.domain.models.claimantresponse.ResponseAcceptation;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.util.function.Predicate;

public class ClaimantResponseUtils {

    private ClaimantResponseUtils() {
    }

    public static boolean isCompanyOrOrganisationWithCCJDetermination(
        Claim claim,
        ResponseAcceptation responseAcceptation
    ) {
        Response response = claim.getResponse().orElseThrow(IllegalStateException::new);

        return PartyUtils.isCompanyOrOrganisation(response.getDefendant())
            && responseAcceptation.getFormaliseOption()
            .filter(Predicate.isEqual(FormaliseOption.REFER_TO_JUDGE)).isPresent();
    }
}
