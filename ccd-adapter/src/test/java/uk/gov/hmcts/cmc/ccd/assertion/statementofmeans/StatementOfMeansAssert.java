package uk.gov.hmcts.cmc.ccd.assertion.statementofmeans;

import org.assertj.core.api.AbstractAssert;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDBankAccount;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDCourtOrder;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDDebt;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDExpense;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDIncome;
import uk.gov.hmcts.cmc.ccd.domain.statementofmeans.CCDStatementOfMeans;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.BankAccount;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.CourtOrder;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Debt;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.Income;
import uk.gov.hmcts.cmc.domain.models.statementofmeans.StatementOfMeans;

import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.cmc.ccd.assertion.Assertions.assertThat;

public class StatementOfMeansAssert extends AbstractAssert<StatementOfMeansAssert, StatementOfMeans> {

    public StatementOfMeansAssert(StatementOfMeans actual) {
        super(actual, StatementOfMeansAssert.class);
    }

    public StatementOfMeansAssert isEqualTo(CCDStatementOfMeans ccdStatementOfMeans) {
        isNotNull();

        if (!Objects.equals(actual.getReason(), ccdStatementOfMeans.getReason())) {
            failWithMessage("Expected StatementOfMeans.reason to be <%s> but was <%s>",
                ccdStatementOfMeans.getReason(), actual.getReason());
        }

        if (!Objects.equals(actual.getResidence().getType().name(),
            ccdStatementOfMeans.getResidence().getType().name())) {
            failWithMessage("Expected StatementOfMeans.residence.type to be <%s> but was <%s>",
                ccdStatementOfMeans.getResidence().getType(), actual.getResidence().getType());
        }

        actual.getEmployment()
            .ifPresent(employment -> assertThat(employment).isEqualTo(ccdStatementOfMeans.getEmployment()));
        actual.getDependant()
            .ifPresent(dependant -> assertThat(dependant).isEqualTo(ccdStatementOfMeans.getDependant()));

        actual.getCourtOrders()
            .forEach(courtOrder -> assertCourtOrder(courtOrder, ccdStatementOfMeans.getCourtOrders()));

        actual.getBankAccounts()
            .forEach(bankAccount -> assertBankAccount(bankAccount, ccdStatementOfMeans.getBankAccounts()));

        actual.getDebts()
            .forEach(debt -> assertDebt(debt, ccdStatementOfMeans.getDebts()));

        actual.getIncomes()
            .forEach(income -> assertIncome(income, ccdStatementOfMeans.getIncomes()));

        actual.getExpenses()
            .forEach(expense -> assertExpense(expense, ccdStatementOfMeans.getExpenses()));

        return this;
    }

    private void assertExpense(Expense expense, List<CCDCollectionElement<CCDExpense>> ccdExpenses) {
        ccdExpenses.stream()
            .map(CCDCollectionElement::getValue)
            .filter(ccdExpense -> expense.getType().equals(ccdExpense.getType()))
            .findFirst()
            .ifPresent(ccdExpense -> assertThat(expense).isEqualTo(ccdExpense));
    }

    private void assertIncome(Income income, List<CCDCollectionElement<CCDIncome>> ccdIncomes) {
        ccdIncomes.stream()
            .map(CCDCollectionElement::getValue)
            .filter(ccdIncome -> income.getType().equals(ccdIncome.getType()))
            .findFirst()
            .ifPresent(ccdIncome -> assertThat(income).isEqualTo(ccdIncome));
    }

    private void assertDebt(Debt debt, List<CCDCollectionElement<CCDDebt>> ccdDebts) {
        ccdDebts.stream()
            .map(CCDCollectionElement::getValue)
            .filter(ccdDebt -> debt.getDescription().equals(ccdDebt.getDescription()))
            .findFirst()
            .ifPresent(ccdDebt -> assertThat(debt).isEqualTo(ccdDebt));
    }

    private void assertBankAccount(
        BankAccount bankAccount,
        List<CCDCollectionElement<CCDBankAccount>> ccdBankAccounts
    ) {
        ccdBankAccounts.stream()
            .map(CCDCollectionElement::getValue)
            .filter(
                ccdBankAccount -> bankAccount.getType().name().equals(ccdBankAccount.getType().name())
            )
            .findFirst()
            .ifPresent(ccdBankAccount -> assertThat(bankAccount).isEqualTo(ccdBankAccount));
    }

    private void assertCourtOrder(
        CourtOrder courtOrder,
        List<CCDCollectionElement<CCDCourtOrder>> ccdCourtOrders
    ) {
        ccdCourtOrders.stream()
            .map(CCDCollectionElement::getValue)
            .filter(ccdCourtOrder -> courtOrder.getClaimNumber().equals(ccdCourtOrder.getClaimNumber()))
            .findFirst()
            .ifPresent(ccdCourtOrder -> assertThat(courtOrder).isEqualTo(ccdCourtOrder));
    }
}
