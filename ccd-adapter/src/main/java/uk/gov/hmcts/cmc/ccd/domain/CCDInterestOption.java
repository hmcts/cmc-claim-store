package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDInterestOption {
    BREAKDOWN("breakdown"),
    SAME_RATE("same");

    private String value;

    CCDInterestOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
