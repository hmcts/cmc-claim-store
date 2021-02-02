package uk.gov.hmcts.cmc.ccd.domain;

public enum HwFMoreInfoRequiredDocuments {
    BANK_STATEMENTS("•\t Bank Statement  - A bank statement from the last 3 months \n"),
    WAGE_SLIPS("•\t Wage Slips - If you’re employed, a payslip dated in the last 6 weeks,"
        + " or, if you’re self-employed, your most recent self-assessment tax return and SA302 tax calculation"
        + " which you can get from www.gov.uk/sa302-tax-calculation \n"),
    CHILD_MAINTENANCE("•\t Child Maintenance - Evidence of being in receipt of Child Maintenance, "
        + "such as a Child Support Agency assessment, sealed court order or letter of agreement showing "
        + "how often and much you’re paid \n"),
    BENEFITS_AND_TAX_CREDITS("•\t Benefits and Tax Credits - A letter or document dated in the last 3 months, "
        + "of contribution-based Job Seekers Allowance (JSA) contribution-based Employment "
        + "and Support Allowance (JSA), Universal Credit, Child Benefit, Working Tax Credit or Child Tax Credit \n"),
    PENSIONS("•\t Pensions - Evidence from Pensions \n"),
    RENTAL_INCOME("•\t Rental Income - Evidence from Rental Income \n"),
    INCOME_FROM_SELLING_GOODS("•\t Income from Selling Goods \n"),
    PRISONERS_INCOME("•\t Prisoner's Income  \n"),
    ANY_OTHER_INCOME("Any Other Income");


    private final String description;

    HwFMoreInfoRequiredDocuments(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
