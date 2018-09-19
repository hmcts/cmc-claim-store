package uk.gov.hmcts.cmc.domain.models.statementofmeans;

public enum DisabilityStatus {
    NO("No"),
    SINGLE("Single"),
    COUPLE("Couple"),
    SEVERE_SINGLE("Severe Disability Single"),
    SEVERE_COUPLE("Severe Disability Couple"),
    DISABLED_DEPENDANT("Disabled Dependant"),
    CARER("Carer");

    String description;

    DisabilityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
