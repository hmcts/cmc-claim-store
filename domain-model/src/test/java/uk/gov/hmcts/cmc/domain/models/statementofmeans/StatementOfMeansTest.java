package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Child.AgeGroupType.UNDER_11;

public class StatementOfMeansTest {
    public static StatementOfMeans.StatementOfMeansBuilder newSampleOfStatementOfMeansBuilder() {
        return StatementOfMeans.builder()
                .residence(ResidenceTest.newSampleOfResidenceBuilder().build())
                .dependant(DependantTest.newSampleOfDependantBuilder().build())
                .employment(Employment.builder().unemployment(Unemployment.builder().retired(true).build()).build())
                .bankAccounts(Collections.singletonList(BankAccountTest.newSampleOfBankAccountBuilder().build()))
                .debts(Collections.singletonList(DebtTest.newSampleOfDebtBuilder().build()))
                .incomes(Collections.singletonList(IncomeTest.newSampleOfIncomeBuilder().build()))
                .expenses(Collections.singletonList(ExpenseTest.newSampleOfExpenseBuilder().build()))
                .courtOrders(Collections.singletonList(CourtOrderTest.newSampleOfCourtOrderBuilder().build()))
                .disability(DisabilityStatus.NO)
                .reason("Reason");
    }

    @Test
    public void shouldBeSuccessfulValidationForCorrectStatementOfMeans() {
        //given
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder().build();
        //when
        Set<String> response = validate(statementOfMeans);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        StatementOfMeans statementOfMeans = StatementOfMeans.builder().build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullResidence() {
        //given
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .residence(null)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("residence : may not be null");
    }

    @Test
    public void shouldBeInvalidForInvalidResidence() {
        //given
        Residence invalidResidence = Residence.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .residence(invalidResidence)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("residence.type : may not be null");
    }

    @Test
    public void shouldBeInvalidForInvalidDependant() {
        //given
        Child invalidChild = Child.builder()
                .ageGroupType(UNDER_11)
                .build();
        Dependant invalidDependant = Dependant.builder()
                .children(Collections.singletonList(invalidChild))
                .build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .dependant(invalidDependant)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("dependant.children[0].numberOfChildren : may not be null");
    }

    @Test
    public void shouldBeInvalidForInvalidEmployment() {
        //given
        Employment invalidEmployment = Employment.builder()
                .selfEmployment(SelfEmployment.builder().build())
                .build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .employment(invalidEmployment)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(2)
                .contains("employment.selfEmployment.jobTitle : may not be empty")
                .contains("employment.selfEmployment.annualTurnover : may not be null");
    }

    @Test
    public void shouldBeValidForEmptyBankAccount() {
        //given
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .bankAccounts(Collections.emptyList())
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(0);
    }

    @Test
    public void shouldBeInvalidForInvalidBankAccount() {
        //given
        BankAccount invalidBankAccount = BankAccount.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .bankAccounts(Collections.singletonList(invalidBankAccount))
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(2)
                .contains("bankAccounts[0].type : may not be null")
                .contains("bankAccounts[0].balance : may not be null");
    }

    @Test
    public void shouldBeInvalidForEachNullBankAccount() {
        //given
        List<BankAccount> bankAccounts = new ArrayList<>();
        bankAccounts.add(null);
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .bankAccounts(bankAccounts)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("bankAccounts : each element must be not null");
    }

    @Test
    public void shouldBeInvalidForInvalidDebt() {
        //given
        Debt invalidDebt = Debt.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .debts(Collections.singletonList(invalidDebt))
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(3)
                .contains("debts[0].totalOwed : may not be null")
                .contains("debts[0].monthlyPayments : may not be null")
                .contains("debts[0].description : may not be empty");
    }

    @Test
    public void shouldBeInvalidForEachNullDebt() {
        //given
        List<Debt> debts = new ArrayList<>();
        debts.add(null);
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .debts(debts)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("debts : each element must be not null");
    }

    @Test
    public void shouldBeInvalidForInvalidIncome() {
        //given
        Income invalidIncome = Income.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .incomes(Collections.singletonList(invalidIncome))
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(3)
                .contains("incomes[0].type : may not be null")
                .contains("incomes[0].amount : may not be null")
                .contains("incomes[0].frequency : may not be null");
    }

    @Test
    public void shouldBeInvalidForEachNullIncome() {
        //given
        List<Income> incomes = new ArrayList<>();
        incomes.add(null);
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .incomes(incomes)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("incomes : each element must be not null");
    }

    @Test
    public void shouldBeInvalidForInvalidExpense() {
        //given
        Expense invalidExpense = Expense.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .expenses(Collections.singletonList(invalidExpense))
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(3)
                .contains("expenses[0].amount : may not be null")
                .contains("expenses[0].type : may not be null")
                .contains("expenses[0].frequency : may not be null");
    }

    @Test
    public void shouldBeInvalidForEachNullExpense() {
        //given
        List<Expense> expenses = new ArrayList<>();
        expenses.add(null);
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .expenses(expenses)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("expenses : each element must be not null");
    }

    @Test
    public void shouldBeInvalidForInvalidCourtOrder() {
        //given
        CourtOrder invalidCourtOrder = CourtOrder.builder().build();
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .courtOrders(Collections.singletonList(invalidCourtOrder))
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(3)
                .contains("courtOrders[0].amountOwed : may not be null")
                .contains("courtOrders[0].monthlyInstalmentAmount : may not be null")
                .contains("courtOrders[0].claimNumber : may not be empty");
    }

    @Test
    public void shouldBeInvalidForEachNullCourtOrder() {
        //given
        List<CourtOrder> courtOrders = new ArrayList<>();
        courtOrders.add(null);
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .courtOrders(courtOrders)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("courtOrders : each element must be not null");
    }

    @Test
    public void shouldBeInvalidForNullReason() {
        //given
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .reason(null)
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("reason : may not be empty");
    }

    @Test
    public void shouldBeInvalidForBlankReason() {
        //given
        StatementOfMeans statementOfMeans = newSampleOfStatementOfMeansBuilder()
                .reason("")
                .build();
        //when
        Set<String> errors = validate(statementOfMeans);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("reason : may not be empty");
    }
}
