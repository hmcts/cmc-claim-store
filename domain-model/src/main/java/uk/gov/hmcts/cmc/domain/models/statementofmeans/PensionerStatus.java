package uk.gov.hmcts.cmc.domain.models.statementofmeans;

public enum PensionerStatus {
    NO("No"),
    SINGLE("Single"),
    COUPLE("Couple");

    String description;

    PensionerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
