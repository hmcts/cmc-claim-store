package uk.gov.hmcts.cmccase.models.ccj;

public enum PaymentSchedule {
    EACH_WEEK("each week"),
    EVERY_TWO_WEEKS("every two weeks"),
    EVERY_MONTH("every month");

    private String description;

    PaymentSchedule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
