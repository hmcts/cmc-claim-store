package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDInterestType {
    STANDARD("standard"),
    DIFFERENT("different"),
    NO_INTEREST("no interest");

    private String value;

    CCDInterestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
