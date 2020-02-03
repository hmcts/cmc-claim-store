package uk.gov.hmcts.cmc.domain.models.statementofmeans;

public enum PaymentFrequency {
    WEEK("Week"),
    TWO_WEEKS("2 Weeks"),
    FOUR_WEEKS("4 Weeks"),
    MONTH("Month");

    private final String description;

    PaymentFrequency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
