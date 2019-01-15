package uk.gov.hmcts.cmc.ccd.domain.offers;

public enum CCDStatementType {
    OFFER("offer"),
    ACCEPTATION("acceptation"),
    REJECTION("rejection"),
    COUNTERSIGNATURE("counter signature");

    private final String value;

    CCDStatementType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
