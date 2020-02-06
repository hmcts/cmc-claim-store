package uk.gov.hmcts.cmc.ccd.domain.defendant;

public enum CCDDefenceType {
    DISPUTE("dispute"),
    ALREADY_PAID("already paid");

    private final String value;

    CCDDefenceType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
