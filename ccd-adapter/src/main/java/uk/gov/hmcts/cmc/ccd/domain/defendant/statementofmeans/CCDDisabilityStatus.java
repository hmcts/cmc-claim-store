package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDDisabilityStatus {
    NO("No"),
    YES("Yes"),
    SEVERE("Severe");

    String description;

    CCDDisabilityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
