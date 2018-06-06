package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import uk.gov.hmcts.cmc.domain.models.response.YesNoOption;

import java.math.BigDecimal;
import java.util.Objects;

import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class BankAccount {

    public enum BankAccountType {
        CURRENT_ACCOUNT("Current account"),
        SAVING_ACCOUNT("Saving account"),
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

    private final BankAccountType typeOfAccount;
    private final YesNoOption jointOption;
    private final BigDecimal balance;

    public BankAccount(BankAccountType typeOfAccount, YesNoOption jointOption, BigDecimal balance) {
        this.typeOfAccount = typeOfAccount;
        this.jointOption = jointOption;
        this.balance = balance;
    }

    public BankAccountType getTypeOfAccount() {
        return typeOfAccount;
    }

    public YesNoOption getJointOption() {
        return jointOption;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BankAccount bankAccount = (BankAccount) other;
        return Objects.equals(typeOfAccount, bankAccount.typeOfAccount)
            && Objects.equals(jointOption, bankAccount.jointOption)
            && Objects.equals(balance, bankAccount.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeOfAccount, jointOption, balance);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
