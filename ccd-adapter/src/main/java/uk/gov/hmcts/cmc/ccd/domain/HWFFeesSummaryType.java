package uk.gov.hmcts.cmc.ccd.domain;

public enum HWFFeesSummaryType {
    NOT_QUALIFY_FEE_ASSISTANCE("NOT_QUALIFY_FEE_ASSISTANCE"),
    INCORRECT_EVIDENCE("INCORRECT_EVIDENCE"),
    INSUFFICIENT_EVIDENCE("INSUFFICIENT_EVIDENCE"),
    FEES_REQUIREMENT_NOT_MET("FEES_REQUIREMENT_NOT_MET");


    private final String description;

    HWFFeesSummaryType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
