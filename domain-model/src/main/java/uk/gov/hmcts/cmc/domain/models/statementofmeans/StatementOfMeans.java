package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
public class StatementOfMeans {
    private final ResidenceType residenceType;
    private final Dependant dependant;
    private final Employment employment;
    private final List<BankAccount> bankAccounts;
    private final List<Debt> debts;
    private final List<Income> incomes;
    private final List<Expense> expenses;
    private final List<CourtOrder> courtOrders;
    private final String reason;

    public StatementOfMeans(
        ResidenceType residenceType,
        Dependant dependant,
        Employment employment,
        List<BankAccount> bankAccounts,
        List<Debt> debts,
        List<Income> incomes,
        List<Expense> expenses,
        List<CourtOrder> courtOrders,
        String reason
    ) {
        this.residenceType = residenceType;
        this.dependant = dependant;
        this.employment = employment;
        this.bankAccounts = bankAccounts;
        this.debts = debts;
        this.incomes = incomes;
        this.expenses = expenses;
        this.courtOrders = courtOrders;
        this.reason = reason;
    }

    public ResidenceType getResidenceType() {
        return residenceType;
    }

    public Optional<Dependant> getDependant() {
        return Optional.ofNullable(dependant);
    }

    public Optional<Employment> getEmployment() {
        return Optional.ofNullable(employment);
    }

    public List<BankAccount> getBankAccounts() {
        return bankAccounts != null ? bankAccounts : emptyList();
    }

    public List<Debt> getDebts() {
        return debts != null ? debts : emptyList();
    }

    public List<Income> getIncomes() {
        return incomes != null ? incomes : emptyList();
    }

    public List<Expense> getExpenses() {
        return expenses != null ? expenses : emptyList();
    }

    public List<CourtOrder> getCourtOrders() {
        return courtOrders != null ? courtOrders : emptyList();
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        StatementOfMeans that = (StatementOfMeans) other;
        return residenceType == that.residenceType
            && Objects.equals(dependant, that.dependant)
            && Objects.equals(employment, that.employment)
            && Objects.equals(bankAccounts, that.bankAccounts)
            && Objects.equals(debts, that.debts)
            && Objects.equals(incomes, that.incomes)
            && Objects.equals(expenses, that.expenses)
            && Objects.equals(courtOrders, that.courtOrders)
            && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(residenceType, dependant, employment, bankAccounts, debts, incomes, expenses,
            courtOrders, reason);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
