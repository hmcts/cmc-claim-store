package uk.gov.hmcts.cmc.ccd.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CCDYesNoOption {
    YES("Yes"),
    NO("No");

    private String value;

    CCDYesNoOption(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean toBoolean() {
        return this == YES;
    }

    public static CCDYesNoOption valueOf(boolean value) {
        return value ? YES : NO;
    }
}
