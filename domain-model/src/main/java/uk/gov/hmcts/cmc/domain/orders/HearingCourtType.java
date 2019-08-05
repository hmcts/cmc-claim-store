package uk.gov.hmcts.cmc.domain.orders;

public enum HearingCourtType {
    EDMONTON("N182TN"),
    MANCHESTER("M609DJ"),
    BIRMINGHAM("B11AA"),
    CLERKENWELL("EC1V3RE");

    private final String value;

    HearingCourtType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
