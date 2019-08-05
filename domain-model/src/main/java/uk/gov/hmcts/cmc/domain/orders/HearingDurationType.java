package uk.gov.hmcts.cmc.domain.orders;

public enum HearingDurationType {
    ONE_DAY("One day"),
    FOUR_HOURS("Four hours"),
    THREE_HOURS("Three hours"),
    TWO_HOURS("Two hours"),
    HALF_HOUR("30 minutes"),
    ONE_AND_HALF_HOUR("One and half hour"),
    ONE_HOUR("One hour");

    private final String value;

    HearingDurationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
