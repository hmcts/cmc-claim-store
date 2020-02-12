package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDInterestEndDateType {
    SETTLED_OR_JUDGMENT("settled_or_judgment"),
    SUBMISSION("submission");

    private final String value;

    CCDInterestEndDateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
