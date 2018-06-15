package uk.gov.hmcts.cmc.ccd.domain.ccj;

public enum CCDPaymentSchedule {
    EACH_WEEK("Each week"),
    EVERY_TWO_WEEKS("Every two weeks"),
    EVERY_MONTH("Every month");

    private String description;

    CCDPaymentSchedule(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
