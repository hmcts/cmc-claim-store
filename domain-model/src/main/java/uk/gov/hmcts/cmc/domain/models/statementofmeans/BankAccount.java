package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.constraints.Money;
import uk.gov.hmcts.cmc.domain.models.CollectionId;

import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@EqualsAndHashCode(callSuper = true)
public class BankAccount extends CollectionId {

    public enum BankAccountType {
        CURRENT_ACCOUNT("Current account"),
        SAVINGS_ACCOUNT("Savings account"),
        ISA("ISA"),
        OTHER("Other");

        private final String description;

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

    @Builder
    public BankAccount(String id, BankAccountType type, boolean joint, BigDecimal balance) {
        super(id);
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
