package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks;

import uk.gov.hmcts.cmc.claimstore.exceptions.CallbackException;

import java.util.Arrays;

public enum CallbackType {
    ABOUT_TO_START("about-to-start"),
    ABOUT_TO_SUBMIT("about-to-submit"),
    SUBMITTED("submitted"),
    MID("mid");

    private final String value;

    CallbackType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CallbackType fromValue(String value) {
        return Arrays.stream(values()).filter(event -> event.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new CallbackException("Unknown Callback Type: " + value));
    }
}
