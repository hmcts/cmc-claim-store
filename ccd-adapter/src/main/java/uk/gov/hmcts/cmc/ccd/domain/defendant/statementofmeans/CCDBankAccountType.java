package uk.gov.hmcts.cmc.ccd.domain.defendant.statementofmeans;

public enum CCDBankAccountType {
    CURRENT_ACCOUNT("Current account"),
    SAVINGS_ACCOUNT("Savings account"),
    ISA("ISA"),
    OTHER("Other");

    String description;

    CCDBankAccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
