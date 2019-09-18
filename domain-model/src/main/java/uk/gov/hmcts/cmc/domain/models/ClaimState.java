package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;

public enum ClaimState {
    CREATE("create"),
    OPEN("open"),
    CLOSED("closed"),
    SETTLED("settled"),
    READY_FOR_DIRECTIONS("readyForDirections"),
    STAYED("stayed"),
    ORDER_FOR_JUDGE_REVIEW("orderForJudgeReview"),
    ORDER_FOR_LA_REVIEW("orderForLAReview"),
    ORDER_DRAWN("orderDrawn"),
    TRANSFERRED("transferred"),
    APPROVED("approved"),
    READY_FOR_TRANSFER("readyForTransfer"),
    RECONSIDERATION_REQUESTED("reconsiderationRequested"),
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
