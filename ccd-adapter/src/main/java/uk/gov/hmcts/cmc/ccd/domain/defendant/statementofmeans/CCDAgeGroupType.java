package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDAgeGroupType {
    UNDER_11("Under 11"),
    BETWEEN_11_AND_15("Between 11 and 15"),
    BETWEEN_16_AND_19("Between 16 and 19");

    String description;

    CCDAgeGroupType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
