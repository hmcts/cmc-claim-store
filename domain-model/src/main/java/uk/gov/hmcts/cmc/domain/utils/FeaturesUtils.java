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

    public static boolean isOnlineDQ(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(ClaimFeatures.DQ_FLAG.getValue());
    }

    public static boolean isLegalAdvisorPilot(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(ClaimFeatures.LA_PILOT_FLAG.getValue());
    }

    public static boolean isJudgePilot(Claim claim) {
        return claim.getFeatures() != null && claim.getFeatures().contains(ClaimFeatures.JUDGE_PILOT_FLAG.getValue());
    }
}
