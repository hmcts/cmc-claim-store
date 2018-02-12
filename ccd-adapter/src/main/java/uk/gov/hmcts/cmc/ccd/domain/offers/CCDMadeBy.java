package uk.gov.hmcts.cmc.ccd.domain.offers;

public enum CCDMadeBy {
    CLAIMANT("claimant"),
    DEFENDANT("defendant");

    private String value;

    CCDMadeBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
