package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.roboticssupport;

import java.util.Arrays;

public enum RpaEventType {
    CLAIM,
    MORE_TIME,
    CCJ,
    DEFENDANT_RESPONSE,
    PAID_IN_FULL;

    public static RpaEventType fromValue(String value) {
        return Arrays.stream(values())
            .filter(event -> String.valueOf(event).equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}
