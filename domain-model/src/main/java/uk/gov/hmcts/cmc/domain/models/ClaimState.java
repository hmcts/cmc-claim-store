package uk.gov.hmcts.cmc.domain.models;

public enum ClaimState {
    CREATED("created"),
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
}
