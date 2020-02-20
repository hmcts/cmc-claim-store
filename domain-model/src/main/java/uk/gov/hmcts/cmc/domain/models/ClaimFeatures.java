package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;

public enum ClaimFeatures {
    ADMISSIONS("admissions"),
    DQ_FLAG("directionsQuestionnaire"),
    LA_PILOT_FLAG("LAPilotEligible"),
    JUDGE_PILOT_FLAG("judgePilotEligible"),
    MEDIATION_PILOT("mediationPilot");

    private final String state;

    ClaimFeatures(String state) {
        this.state = state;
    }

    public String getValue() {
        return state;
    }

    public static ClaimFeatures fromValue(String value) {
        return Arrays.stream(ClaimFeatures.values())
            .filter(state -> state.state.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ClaimFeatures: " + value));
    }
}
