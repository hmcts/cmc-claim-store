package uk.gov.hmcts.cmc.domain.models.statementofmeans;

public enum DisabilityStatus {
    NO("No"),
    YES("Yes"),
    SEVERE("Severe");

    String description;

    DisabilityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
