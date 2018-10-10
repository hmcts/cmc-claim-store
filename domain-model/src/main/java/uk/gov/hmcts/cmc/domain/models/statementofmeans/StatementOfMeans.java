package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.hmcts.cmc.domain.constraints.EachNotNull;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.cmc.domain.utils.ToStringStyle.ourStyle;

@Builder
@EqualsAndHashCode
public class StatementOfMeans {

    @Valid
    @NotNull
    private final Residence residence;

    @Valid
    private final Dependant dependant;

    @Valid
    @NotNull
    private final Employment employment;

    @Valid
    @NotEmpty
    @EachNotNull
    private final List<BankAccount> bankAccounts;

    @Valid
    @EachNotNull
    private final List<Debt> debts;

    @Valid
    @EachNotNull
    private final List<Income> incomes;

    @Valid
    @EachNotNull
    private final List<Expense> expenses;

    @Valid
    @EachNotNull
    private final List<CourtOrder> courtOrders;

    @Valid
    @EachNotNull
    private final List<PriorityDebt> priorityDebts;

    @Valid
    private final LivingPartner partner;

    private final DisabilityStatus disability;

    private final boolean carer;
  
    @NotBlank
    private final String reason;

    public StatementOfMeans(
        Residence residence,
        Dependant dependant,
        Employment employment,
        List<BankAccount> bankAccounts,
        List<Debt> debts,
        List<Income> incomes,
        List<Expense> expenses,
        List<CourtOrder> courtOrders,
        List<PriorityDebt> priorityDebts,
        LivingPartner partner,
        DisabilityStatus disability,
        boolean carer,
        String reason
    ) {
        this.residence = residence;
        this.dependant = dependant;
        this.employment = employment;
        this.bankAccounts = bankAccounts;
        this.debts = debts;
        this.incomes = incomes;
        this.expenses = expenses;
        this.courtOrders = courtOrders;
        this.priorityDebts = priorityDebts;
        this.partner = partner;
        this.disability = disability;
        this.carer = carer;
        this.reason = reason;
    }

    public Residence getResidence() {
        return residence;
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

    public List<PriorityDebt> getPriorityDebts() {
        return priorityDebts != null ? priorityDebts : emptyList();
    }

    public Optional<LivingPartner> getPartner() {
        return Optional.ofNullable(partner);
    }

    public Optional<DisabilityStatus> getDisability() {
        return Optional.ofNullable(disability);
    }

    public boolean isCarer() {
        return carer;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ourStyle());
    }
}
