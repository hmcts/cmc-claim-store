package uk.gov.hmcts.cmc.claimstore.utils;

import uk.gov.hmcts.cmc.domain.models.otherparty.CompanyDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.IndividualDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.OrganisationDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.SoleTraderDetails;
import uk.gov.hmcts.cmc.domain.models.otherparty.TheirDetails;
import java.util.Objects;

public class TheirDetailsHelper {

    private TheirDetailsHelper() {
        // Utility class
    }

    public static boolean isDefendantBusiness(TheirDetails theirDetails) {
        Objects.requireNonNull(theirDetails);
        return theirDetails instanceof CompanyDetails || theirDetails instanceof OrganisationDetails;
    }

    public static boolean isDefendantIndividual(TheirDetails theirDetails) {
        Objects.requireNonNull(theirDetails);
        return theirDetails instanceof IndividualDetails;
    }

    public static boolean isDefendantSoleTrader(TheirDetails theirDetails) {
        Objects.requireNonNull(theirDetails);
        return theirDetails instanceof SoleTraderDetails;
    }
}
