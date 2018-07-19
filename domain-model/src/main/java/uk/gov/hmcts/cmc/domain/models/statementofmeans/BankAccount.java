package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class BankAccount {

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

    @NotNull
    private final BankAccountType type;

    private final boolean joint;

    @Money
    @NotNull
    private final BigDecimal balance;

    public BankAccount(BankAccountType type, boolean joint, BigDecimal balance) {
        this.type = type;
        this.joint = joint;
        this.balance = balance;
    }

    public BankAccountType getType() {
        return type;
    }

    public boolean isJoint() {
        return joint;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
