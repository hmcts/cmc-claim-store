package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;

public enum ClaimState {
    CREATE("create"),
    OPEN("open"),
    CLOSED("closed"),
    SETTLED("settled"),
    ORDER_DRAWN("orderDrawn"),
    AWAITING_CITIZEN_PAYMENT("awaitingCitizenPayment");

    private final String state;

    ClaimState(String state) {
        this.state = state;
    }

    public String getValue() {
        return state;
    }

    public static ClaimState fromValue(String value) {
        return Arrays.stream(ClaimState.values())
            .filter(val -> val.name().equalsIgnoreCase(value))
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }
}
