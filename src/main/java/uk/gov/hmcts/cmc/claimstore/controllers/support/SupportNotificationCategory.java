package uk.gov.hmcts.cmc.claimstore.controllers.support;

import java.util.Arrays;

public enum SupportNotificationCategory {
    CLAIM("claim"),
    MORE_TIME("more-time"),
    RESPONSE("response"),
    CCJ("ccj"),
    SETTLEMENT("settlement"),
    CLAIMANT_RESPONSE("claimant-response"),
    INTENT_TO_PROCEED("intent-to-proceed"),
    PAID_IN_FULL("paid-in-full");

    private final String value;

    SupportNotificationCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SupportNotificationCategory fromValue(String value) {
        return Arrays.stream(values())
            .filter(category -> category.value.equals(value))
            .findAny()
            .orElseThrow(() -> new IllegalArgumentException("Unknown category name: " + value));
    }
}
