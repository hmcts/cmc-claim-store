package uk.gov.hmcts.cmc.domain.models.orders;

public enum HearingDurationType {
    ONE_DAY("One day"),
    FOUR_HOURS("Four hours"),
    THREE_HOURS("Three hours"),
    TWO_AND_A_HALF_HOURS("Two and a half hours"),
    TWO_HOURS("Two hours"),
    ONE_AND_A_HALF_HOURS("One and a half hours"),
    ONE_HOUR("One hour"),
    HALF_HOUR("30 minutes");

    private final String value;

    HearingDurationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
