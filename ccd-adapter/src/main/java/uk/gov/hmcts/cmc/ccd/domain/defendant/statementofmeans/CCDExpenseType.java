package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDExpenseType {
    MORTGAGE("Mortgage"),
    RENT("Rent"),
    COUNCIL_TAX("Council Tax"),
    GAS("Gas"),
    ELECTRICITY("Electricity"),
    WATER("Water"),
    TRAVEL("Travel (work or school)"),
    SCHOOL_COSTS("School costs (include clothing)"),
    FOOD_HOUSEKEEPING("Food and housekeeping"),
    TV_AND_BROADBAND("TV and broadband"),
    HIRE_PURCHASES("Hire purchase"),
    MOBILE_PHONE("Mobile phone"),
    MAINTENANCE_PAYMENTS("Maintenance payments"),
    OTHER("Other");

    String description;

    CCDExpenseType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
