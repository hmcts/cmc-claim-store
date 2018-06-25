package uk.gov.hmcts.cmc.claimstore.documents.content;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

@Component
public class StatementOfMeansContentProvider {

    public Map<String, Object> createContent(StatementOfMeans statementOfMeans) {
        requireNonNull(statementOfMeans);

        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        Residence residence = statementOfMeans.getResidence();
        contentBuilder.put("residence", residence);
        contentBuilder.put("residenceTypeDescription", residence.getType().getDescription());

        contentBuilder.putAll(createDependant(statementOfMeans));

        contentBuilder.put("bankAccounts", statementOfMeans.getBankAccounts());
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

    private Map<String, Object> createIncome(Income income) {
        requireNonNull(income);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", income.getType().getDescription())
            .put("amountReceived", income.getAmountReceived())
            .put("frequency", income.getFrequency().getDescription())
            .build();
    }

    public Map<String, Object> createExpense(Expense expense) {
        requireNonNull(expense);

        return new ImmutableMap.Builder<String, Object>()
            .put("type", expense.getType().getDescription())
            .put("amountPaid", expense.getAmountPaid())
            .put("frequency", expense.getFrequency().getDescription())
            .build();
    }

    private Map<String, Object> createEmployment(StatementOfMeans statementOfMeans) {
        ImmutableMap.Builder<String, Object> contentBuilder = ImmutableMap.builder();

        Optional<Employment> optionalEmployment = statementOfMeans.getEmployment();
        if (optionalEmployment.isPresent()) {
            Employment employment = optionalEmployment.get();
            contentBuilder.put("employment", employment);
            if (employment.getSelfEmployment().isPresent()) {
                Optional<SelfEmployment> optionalSelfEmployment = employment.getSelfEmployment();
                if (optionalSelfEmployment.isPresent()) {
                    SelfEmployment selfEmployment = optionalSelfEmployment.get();
                    contentBuilder.put("selfEmployment", selfEmployment);
                    Optional<OnTaxPayments> optionalTaxPayments = selfEmployment.getOnTaxPayments();
                    OnTaxPayments onTaxPayments = optionalTaxPayments.get();
                    contentBuilder.put("onTaxPayments", onTaxPayments);
                }
                contentBuilder.put("jobType", createJobType(employment));
            }
        }
        return contentBuilder.build();
    }

    private Map<String, Object> createDependant(StatementOfMeans statementOfMeans) {
        ImmutableMap.Builder<java.lang.String, java.lang.Object> contentBuilder = ImmutableMap.builder();

        Optional<Dependant> optionalDependant = statementOfMeans.getDependant();
        if (optionalDependant.isPresent()) {
            Dependant dependant = optionalDependant.get();
            contentBuilder.put("dependant", dependant);
            contentBuilder.put("children", dependant.getChildren()
                .stream()
                .map(this::createChild)
                .collect(toList())
            );

            Optional<OtherDependants> optionalOtherDependants = dependant.getOtherDependants();
            if (optionalOtherDependants.isPresent()) {
                OtherDependants otherDependants = optionalOtherDependants.get();
                contentBuilder.put("otherDependants", otherDependants);
            }
            Optional<Integer> optionalMaintainedChildren = dependant.getNumberOfMaintainedChildren();
            Integer maintainedChildren = optionalMaintainedChildren.get();
            contentBuilder.put("maintainedChildren", maintainedChildren);
        }

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
