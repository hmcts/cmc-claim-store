package uk.gov.hmcts.cmc.claimstore.documents.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Child;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Dependant;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Employment;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.OnTaxPayments;
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

        List<BankAccount> bankAccounts = null;
        List<CourtOrder> courtOrders = null;
        List<Expense> expenses = null;
        List<Income> incomes = null;
        List<Child> children = null;
        List<Debt> debts = null;

        Residence residence = null;
        RepaymentPlan repaymentPlan = null;
        Employment employment;
        SelfEmployment selfEmployment;
        OnTaxPayments onTaxPayments = null;
        Integer maintainedChildren = null;

        residence = statementOfMeans.getResidence();
        content.put("residence", residence);

        Optional<Dependant> optionalDependant = statementOfMeans.getDependant();
        if (optionalDependant.isPresent()) {
            Dependant dependant = optionalDependant.get();
            children = dependant.getChildren();
            content.put("children", children);
            Optional<Integer> optionalMaintainedChildren = dependant.getNumberOfMaintainedChildren();
            maintainedChildren = optionalMaintainedChildren.get();
            content.put("maintainedChildren", maintainedChildren);
        }
        bankAccounts = statementOfMeans.getBankAccounts();
        courtOrders = statementOfMeans.getCourtOrders();
        expenses = statementOfMeans.getExpenses();
        incomes = statementOfMeans.getIncomes();
        debts = statementOfMeans.getDebts();

        Optional<Employment> optionalEmployment = statementOfMeans.getEmployment();
        if (optionalEmployment.isPresent()) {
            employment = optionalEmployment.get();
            if (employment.getSelfEmployment().isPresent()) {
                Optional<SelfEmployment> optionalSelfEmployment = employment.getSelfEmployment();
                if (optionalSelfEmployment.isPresent()) {
                    selfEmployment = optionalSelfEmployment.get();
                    Optional<OnTaxPayments> optionalTaxPayments = selfEmployment.getOnTaxPayments();
                    onTaxPayments = optionalTaxPayments.get();
                    content.put("onTaxPayments", onTaxPayments);
                }
            }
        }

        content.put("bankAccounts", bankAccounts);
        content.put("expenses", expenses);
        content.put("incomes", incomes);
        content.put("courtOrders", courtOrders);
        content.put("debts", debts);

        return content;
    }
}
