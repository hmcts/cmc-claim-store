package uk.gov.hmcts.cmc.ccd.domain.ccj;

public enum CCDPaymentSchedule {
    EACH_WEEK("each week"),
    EVERY_TWO_WEEKS("every two weeks"),
    EVERY_MONTH("every month");

    private String description;

    CCDPaymentSchedule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
