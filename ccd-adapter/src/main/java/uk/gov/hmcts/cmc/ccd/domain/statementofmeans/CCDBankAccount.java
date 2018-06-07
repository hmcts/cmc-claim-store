package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Value
@Builder
public class CCDBankAccount {

    public enum BankAccountType {
        CURRENT_ACCOUNT("Current account"),
        SAVINGS_ACCOUNT("Savings account"),
        ISA("ISA"),
        OTHER("Other");

        String description;

        BankAccountType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private BankAccountType typeOfAccount;
    private CCDYesNoOption isJoint;
    private BigDecimal balance;
}
