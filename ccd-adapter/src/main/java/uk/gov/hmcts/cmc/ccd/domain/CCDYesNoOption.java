package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDYesNoOption {
    YES("yes"),
    NO("no");

    private final String value;

    CCDYesNoOption(String value) {
        this.value = value;
    }

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
