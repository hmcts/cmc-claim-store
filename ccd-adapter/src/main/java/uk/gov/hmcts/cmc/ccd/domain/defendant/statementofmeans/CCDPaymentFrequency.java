package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDPaymentFrequency {
    WEEK("Week"),
    TWO_WEEKS("2 Weeks"),
    FOUR_WEEKS("4 Weeks"),
    MONTH("Month");

    String description;

    CCDPaymentFrequency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
