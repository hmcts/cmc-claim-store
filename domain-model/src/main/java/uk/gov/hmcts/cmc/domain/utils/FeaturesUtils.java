package uk.gov.hmcts.cmc.domain.utils;

import uk.gov.hmcts.cmc.domain.models.Claim;

public class FeaturesUtils {
    public static final String MEDIATION_PILOT = "mediationPilot";

    private FeaturesUtils() {
        // utility class, no instances
    }

    public static boolean hasMediationPilotFeature(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(MEDIATION_PILOT);
    }
}
