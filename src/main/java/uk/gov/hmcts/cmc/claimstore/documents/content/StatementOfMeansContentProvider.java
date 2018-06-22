package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OtherDependants;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Residence;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.SelfEmployment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Component
public class StatementOfMeansContentProvider {

    public Map<String, Object> createContent(StatementOfMeans statementOfMeans) {
        requireNonNull(statementOfMeans);

        Map<String, Object> content = new HashMap<>();

        Residence residence = statementOfMeans.getResidence();
        content.put("residence", residence);

        Optional<Dependant> optionalDependant = statementOfMeans.getDependant();
        if (optionalDependant.isPresent()) {
            Dependant dependant = optionalDependant.get();
            content.put("dependant", dependant);
            List<Child> children = dependant.getChildren();
            content.put("children", children);
            Optional<OtherDependants> optionalOtherDependants = dependant.getOtherDependants();
            if (optionalOtherDependants.isPresent()) {
                OtherDependants otherDependants = optionalOtherDependants.get();
                content.put("otherDependants", otherDependants);
            }
            Optional<Integer> optionalMaintainedChildren = dependant.getNumberOfMaintainedChildren();
            Integer maintainedChildren = optionalMaintainedChildren.get();
            content.put("maintainedChildren", maintainedChildren);
        }
        List<BankAccount> bankAccounts = statementOfMeans.getBankAccounts();
        content.put("bankAccounts", bankAccounts);
        List<CourtOrder> courtOrders = statementOfMeans.getCourtOrders();
        content.put("courtOrders", courtOrders);
        List<Expense> expenses = statementOfMeans.getExpenses();
        content.put("expenses", expenses);
        List<Income> incomes = statementOfMeans.getIncomes();
        content.put("incomes", incomes);
        List<Debt> debts = statementOfMeans.getDebts();
        content.put("debts", debts);

        Optional<Employment> optionalEmployment = statementOfMeans.getEmployment();
        if (optionalEmployment.isPresent()) {
            Employment employment = optionalEmployment.get();
            content.put("employment", employment);
            if (employment.getSelfEmployment().isPresent()) {
                Optional<SelfEmployment> optionalSelfEmployment = employment.getSelfEmployment();
                if (optionalSelfEmployment.isPresent()) {
                    SelfEmployment selfEmployment = optionalSelfEmployment.get();
                    content.put("selfEmployment", selfEmployment);
                    Optional<OnTaxPayments> optionalTaxPayments = selfEmployment.getOnTaxPayments();
                    OnTaxPayments onTaxPayments = optionalTaxPayments.get();
                    content.put("onTaxPayments", onTaxPayments);
                }
                content.put("jobType", createJobType(employment));
            }
        }
        return content;
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
