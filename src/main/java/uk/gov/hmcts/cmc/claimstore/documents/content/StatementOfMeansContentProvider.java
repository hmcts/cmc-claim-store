package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatMoney;

@Component
public class StatementOfMeansContentProvider {

    public Map<String, Object> createContent(StatementOfMeans statementOfMeans) {
        requireNonNull(statementOfMeans);

        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        Residence residence = statementOfMeans.getResidence();
        contentBuilder.put("residence", residence);
        contentBuilder.put("residenceTypeDescription", residence.getType().getDescription());

        contentBuilder.putAll(createDependant(statementOfMeans));

        contentBuilder.put("bankAccounts", statementOfMeans.getBankAccounts()
            .stream()
            .map(this::createBankAccount)
            .collect(toList())
        );

        contentBuilder.put("courtOrders", statementOfMeans.getCourtOrders());
        contentBuilder.put("debts", statementOfMeans.getDebts());

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

        return contentBuilder.build();
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
            .put("type", income.getType().getDescription())
            .put("amountReceived", formatMoney(income.getAmountReceived()))
            .put("frequency", income.getFrequency().getDescription())
            .build();
    }

    private Map<String, Object> createExpense(Expense expense) {
        requireNonNull(expense);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", expense.getType().getDescription())
            .put("amountPaid", formatMoney(expense.getAmountPaid()))
            .put("frequency", expense.getFrequency().getDescription())
            .build();
    }

    private Map<String, Object> createEmployment(StatementOfMeans statementOfMeans) {
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        statementOfMeans.getEmployment().ifPresent(employment -> {
            contentBuilder.put("jobType", createJobType(employment));
            contentBuilder.put("employment", employment);
            employment.getSelfEmployment().ifPresent(selfEmployment -> {
                contentBuilder.put("selfEmployment", selfEmployment);
                selfEmployment.getOnTaxPayments().ifPresent(onTaxPayments -> {
                    contentBuilder.put("onTaxPayments", createOnTaxPayments(onTaxPayments));
                });
            });
        });

        return contentBuilder.build();
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
            dependant.getOtherDependants().ifPresent(otherDependants -> {
                contentBuilder.put("otherDependants", otherDependants);
            });
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

    public String createJobType(Employment employment) {
        if (employment.getEmployers().size() > 0 && employment.getSelfEmployment().isPresent()) {
            return "Employed and self-employed";
        } else if (employment.getSelfEmployment().isPresent()) {
            return "Self-employed";
        } else if (employment.getEmployers().size() > 0) {
            return "Employed";
        } else {
            return "Unemployed";
        }
    }
}
