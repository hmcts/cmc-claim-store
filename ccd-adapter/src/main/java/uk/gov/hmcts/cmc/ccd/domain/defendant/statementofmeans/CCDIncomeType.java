package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDIncomeType {

    JOB("Income from your job"),
    UNIVERSAL_CREDIT("Universal Credit"),
    JOB_SEEKERS_ALLOWANCE_INCOME_BASES("Jobseeker's Allowance (income based)"),
    JOB_SEEKERS_ALLOWANCE_CONTRIBUTION_BASED("Jobseeker's Allowance (contribution based)"),
    INCOME_SUPPORT("Income Support"),
    WORKING_TAX_CREDIT("Working Tax Credit"),
    CHILD_TAX_CREDIT("Child Tax Credit"),
    CHILD_BENEFIT("Child Benefit"),
    COUNCIL_TAX_SUPPORT("Council Tax Support"),
    PENSION("Pension"),
    OTHER("Other");

    String description;

    CCDIncomeType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

}
