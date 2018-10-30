package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

public enum CCDPriorityDebtType {
    MORTGAGE("Mortgage"),
    RENT("Rent"),
    COUNCIL_TAX_COMMUNITY_CHARGE("Council Tax or Community Charge"),
    GAS("Gas"),
    ELECTRICITY("Electricity"),
    WATER("Water"),
    MAINTENANCE_PAYMENTS("Maintenance Payments");

    String description;

    CCDPriorityDebtType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
