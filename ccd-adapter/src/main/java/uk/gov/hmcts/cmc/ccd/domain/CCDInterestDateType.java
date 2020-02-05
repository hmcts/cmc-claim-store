package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDInterestDateType {
    CUSTOM("custom"),
    SUBMISSION("submission");

    private final String value;

    CCDInterestDateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
