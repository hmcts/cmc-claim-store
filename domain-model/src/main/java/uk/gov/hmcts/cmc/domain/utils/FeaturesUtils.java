package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimFeatures;

public class FeaturesUtils {
    private FeaturesUtils() {
        // utility class, no instances
    }

    public static boolean hasMediationPilotFeature(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(ClaimFeatures.MEDIATION_PILOT.getValue());
    }
}
