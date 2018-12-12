package uk.gov.hmcts.cmc.ccd.domain;

public enum CCDPartyType {
    INDIVIDUAL("Individual"),
    ORGANISATION("Organisation"),
    SOLE_TRADER("SoleTrader"),
    COMPANY("Company");

    private String value;

    CCDPartyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
