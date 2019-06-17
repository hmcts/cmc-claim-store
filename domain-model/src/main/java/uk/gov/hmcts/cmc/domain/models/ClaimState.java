package uk.gov.hmcts.cmc.domain.models;

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
        for (ClaimState claimState : ClaimState.values()) {
            if (claimState.getValue().equalsIgnoreCase(state)) {
                return claimState;
            }
        }
        throw new IllegalArgumentException(state + " is not a valid state.");
    }
}
