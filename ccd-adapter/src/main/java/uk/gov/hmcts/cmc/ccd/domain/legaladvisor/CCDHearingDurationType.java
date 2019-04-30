package uk.gov.hmcts.cmc.ccd.domain.legaladvisor;

// value will be removed once we change the docmosis template
public enum CCDHearingDurationType {
    HALF_HOUR("THIRTY_MIN"),
    ONE_HOUR("ONE_HOUR"),
    ONE_AND_HALF_HOUR("ONE_HALF_HOUR"),
    TWO_HOURS("TWO_HOUR"),
    THREE_HOURS("THREE_HOURS"),
    FOUR_HOURS("FOUR_HOURS"),
    ONE_DAY("ONE_DAY");

    private String value;

    CCDHearingDurationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
