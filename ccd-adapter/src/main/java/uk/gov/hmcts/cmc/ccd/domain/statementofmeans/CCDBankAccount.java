package uk.gov.hmcts.cmc.ccd.domain.statementofmeans;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.cmc.ccd.domain.CCDYesNoOption;

import java.math.BigDecimal;

@Value
@Builder
public class CCDBankAccount {

    private BankAccountType type;
    private CCDYesNoOption joint;
    private BigDecimal balance;

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
}
