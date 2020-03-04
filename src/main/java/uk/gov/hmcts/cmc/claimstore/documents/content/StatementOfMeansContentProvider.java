package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Unemployment;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence.ResidenceType.OTHER;

@Component
public class StatementOfMeansContentProvider {

    public Map<String, Object> createContent(StatementOfMeans statementOfMeans) {
        requireNonNull(statementOfMeans);

        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();
        contentBuilder.put("statementOfMeans", statementOfMeans);

        Residence residence = statementOfMeans.getResidence();
        contentBuilder.put("residence", residence);
        contentBuilder.put("residenceTypeDescription",
            residence.getType() == OTHER
                ? residence.getOtherDetail()
                    .orElseThrow(() -> new IllegalStateException("Missing residence type description"))
                : residence.getType().getDescription());

        contentBuilder.putAll(createDependant(statementOfMeans));

        contentBuilder.put("bankAccounts", statementOfMeans.getBankAccounts()
            .stream()
            .map(this::createBankAccount)
            .collect(toList())
        );

        contentBuilder.put("courtOrders",
            statementOfMeans.getCourtOrders()
                .stream()
                .map(this::createCourtOrder)
                .collect(toList())
        );

        contentBuilder.put("debts",
            statementOfMeans.getDebts()
                .stream()
                .map(this::createDebt)
                .collect(toList())
        );

        contentBuilder.putAll(createEmployment(statementOfMeans));

        contentBuilder.put("expenses",
            statementOfMeans.getExpenses()
                .stream()
                .map(this::createExpense)
                .collect(toList())
        );

        contentBuilder.put("incomes",
            statementOfMeans.getIncomes()
                .stream()
                .map(this::createIncome)
                .collect(toList())
        );

        statementOfMeans.getPartner()
            .ifPresent(
                partner -> contentBuilder.put("partner", partner)
            );

        statementOfMeans.getDisability()
            .ifPresent(
                disability -> contentBuilder.put("disability", disability.getDescription())
            );

        contentBuilder.put("carer", statementOfMeans.isCarer());

        return contentBuilder.build();
    }

    private Map<String, Object> createDebt(Debt debt) {
        requireNonNull(debt);

        return new ImmutableMap.Builder<String, Object>()
            .put("description", debt.getDescription())
            .put("totalOwed", formatMoney(debt.getTotalOwed()))
            .put("monthlyPayments", formatMoney(debt.getMonthlyPayments()))
            .build();
    }

    private Map<String, Object> createCourtOrder(CourtOrder courtOrder) {
        requireNonNull(courtOrder);

        return new ImmutableMap.Builder<String, Object>()
            .put("claimNumber", courtOrder.getClaimNumber())
            .put("monthlyInstalmentAmount", formatMoney(courtOrder.getMonthlyInstalmentAmount()))
            .put("amountOwed", formatMoney(courtOrder.getAmountOwed()))
            .build();
    }

    private Map<String, Object> createBankAccount(BankAccount bankAccount) {
        requireNonNull(bankAccount);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", bankAccount.getType().getDescription())
            .put("joint", bankAccount.isJoint())
            .put("balance", formatMoney(bankAccount.getBalance()))
            .build();
    }

    private Map<String, Object> createIncome(Income income) {
        requireNonNull(income);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", income.getType() == Income.IncomeType.OTHER
                ? income.getOtherSource()
                    .orElseThrow(() -> new IllegalStateException("Missing other income source"))
                : income.getType().getDescription())
            .put("amount", formatMoney(income.getAmount()))
            .put("frequency", income.getFrequency().getDescription())
            .build();
    }

    private Map<String, Object> createExpense(Expense expense) {
        requireNonNull(expense);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", expense.getType() == Expense.ExpenseType.OTHER
                ? expense.getOtherName()
                    .orElseThrow(() -> new IllegalStateException("Missing other expense type name"))
                : expense.getType().getDescription())
            .put("amount", formatMoney(expense.getAmount()))
            .put("frequency", expense.getFrequency().getDescription())
            .build();
    }

    private Map<String, Object> createEmployment(StatementOfMeans statementOfMeans) {
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        statementOfMeans.getEmployment().ifPresent(employment -> {
            contentBuilder.put("jobType", new JobTypeContentProvider().createJobType(employment));
            contentBuilder.put("employment", employment);
            employment.getSelfEmployment().ifPresent(selfEmployment -> {
                contentBuilder.put("selfEmployment", createSelfEmployment(selfEmployment));
                selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments ->
                    contentBuilder.put("onTaxPayments", createOnTaxPayments(onTaxPayments))
                );
            });
        });

        return contentBuilder.build();
    }

    private Map<String, Object> createSelfEmployment(SelfEmployment selfEmployment) {
        return new ImmutableMap.Builder<String, Object>()
            .put("jobTitle", selfEmployment.getJobTitle())
            .put("annualTurnover", formatMoney(selfEmployment.getAnnualTurnover()))
            .build();
    }

    private Map<String, Object> createOnTaxPayments(OnTaxPayments onTaxPayments) {
        return new ImmutableMap.Builder<String, Object>()
            .put("amountYouOwe", formatMoney(onTaxPayments.getAmountYouOwe()))
            .put("reason", onTaxPayments.getReason())
            .build();
    }

    private Map<String, Object> createDependant(StatementOfMeans statementOfMeans) {
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        statementOfMeans.getDependant().ifPresent(dependant -> {
            contentBuilder.put("dependant", dependant);
            contentBuilder.put("children", dependant.getChildren()
                .stream()
                .map(this::createChild)
                .collect(toList())
            );
            dependant.getOtherDependants()
                .ifPresent(otherDependants -> contentBuilder.put("otherDependants", otherDependants));
            dependant.getNumberOfMaintainedChildren().ifPresent(
                maintainedChildren -> contentBuilder.put("maintainedChildren", maintainedChildren)
            );
        });

        return contentBuilder.build();
    }

    private Map<String, Object> createChild(Child child) {
        requireNonNull(child);

        return new ImmutableMap.Builder<String, Object>()
            .put("ageGroupType", child.getAgeGroupType().getDescription())
            .put("numberOfChildren", child.getNumberOfChildren())
            .put("numberOfChildrenLivingWithYou", child.getNumberOfChildrenLivingWithYou())
            .build();
    }

    static class JobTypeContentProvider {
        public String createJobType(Employment employment) {
            if (employment.getEmployers().size() > 0 && employment.getSelfEmployment().isPresent()) {
                return "Employed and self-employed";
            } else if (employment.getSelfEmployment().isPresent()) {
                return "Self-employed";
            } else if (employment.getEmployers().size() > 0) {
                return "Employed";
            } else if (employment.getUnemployment().map(Unemployment::isRetired).orElse(false)) {
                return "Retired";
            } else {
                return "Unemployed";
            }
        }
    }
}
