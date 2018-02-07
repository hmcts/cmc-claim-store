package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDYesNoOption {
    YES("yes"),
    NO("no");

    private String value;

    CCDYesNoOption(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
