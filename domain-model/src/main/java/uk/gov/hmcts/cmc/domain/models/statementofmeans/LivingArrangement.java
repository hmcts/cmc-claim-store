package uk.gov.hmcts.cmc.domain.models.statementofmeans;

public enum LivingArrangement {
    SINGLE_18_24("Single aged 18-24"),
    SINGLE_OVER_25("Single over 25"),
    COUPLES_UNDER_18("Couples under 18"),
    COUPLES_UNDER_18_UNDER_25("Couples one under 18, one under 25"),
    COUPLES_UNDER_18_OVER_25("Couples one under 18, one over 25"),
    COUPLES_OVER_18("Couples over 18");

    String description;

    LivingArrangement(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
