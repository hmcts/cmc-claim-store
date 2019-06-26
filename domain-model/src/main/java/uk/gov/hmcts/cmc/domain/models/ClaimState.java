package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;

public enum ClaimState {
    CREATE("create"),
    OPEN("open"),
    CLOSED("closed"),
    SETTLED("settled");

    private final String state;

    ClaimState(String state) {
        this.state = state;
    }

    public String getValue() {
        return state;
    }

    public static ClaimState fromValue(String state) {
        return Arrays.stream(ClaimState.values())
            .filter(claimState -> claimState.getValue().equalsIgnoreCase(state))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException(state + " is not a valid state."));
    }
}
