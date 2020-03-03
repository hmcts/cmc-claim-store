package uk.gov.hmcts.cmc.domain.models;

import java.util.Arrays;

public enum ClaimState {
    CREATE("create"),
    OPEN("open"),
    CLOSED("closed"),
    SETTLED("settled"),
    READY_FOR_LEGAL_ADVISOR_DIRECTIONS("readyForDirections"),
    READY_FOR_JUDGE_DIRECTIONS("readyForJudgeDirections"),
    STAYED("stayed"),
    ORDER_FOR_JUDGE_REVIEW("orderForJudgeReview"),
    ORDER_FOR_LA_REVIEW("orderForLAReview"),
    ORDER_DRAWN("orderDrawn"),
    TRANSFERRED("transferred"),
    APPROVED("approved"),
    READY_FOR_TRANSFER("readyForTransfer"),
    RECONSIDERATION_REQUESTED("reconsiderationRequested"),
    REFERRED_MEDIATION("referredMediation"),
    AWAITING_CITIZEN_PAYMENT("awaitingCitizenPayment"),
    PROCEEDS_IN_CASE_MAN("proceedsInCaseman");

    private final String state;

    ClaimState(String state) {
        this.state = state;
    }

    public String getValue() {
        return state;
    }

    public static ClaimState fromValue(String value) {
        return Arrays.stream(ClaimState.values())
            .filter(state -> state.state.equalsIgnoreCase(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown ClaimState: " + value));
    }
}
