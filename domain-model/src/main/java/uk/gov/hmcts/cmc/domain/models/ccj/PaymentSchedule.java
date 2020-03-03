package uk.gov.hmcts.cmc.domain.models.ccj;

public enum PaymentSchedule {
    EACH_WEEK("Every week"),
    EVERY_TWO_WEEKS("Every two weeks"),
    EVERY_MONTH("Every month");

    private final String description;

    PaymentSchedule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
