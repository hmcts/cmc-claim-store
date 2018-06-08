package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.MORTGAGE;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

public class ExpenseTest {

    @Test
    public void shouldBeSuccessfulValidationForExpense() {
        //given
        Expense expense = Expense.builder()
                .type(MORTGAGE)
                .otherExpense("Other expense")
                .frequency(MONTH)
                .amountPaid(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> response = validate(expense);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        Expense expense = Expense.builder().build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullType() {
        //given
        Expense expense = Expense.builder()
                .otherExpense("Other expense")
                .frequency(MONTH)
                .amountPaid(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("type : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullPaymentFrequency() {
        //given
        Expense expense = Expense.builder()
                .type(MORTGAGE)
                .otherExpense("Other expense")
                .amountPaid(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("frequency : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullAmountReceived() {
        //given
        Expense expense = Expense.builder()
                .type(MORTGAGE)
                .otherExpense("Other expense")
                .frequency(MONTH)
                .build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountPaid : may not be null");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithMoreThanTwoFractions() {
        //given
        Expense expense = Expense.builder()
                .type(MORTGAGE)
                .otherExpense("Other expense")
                .frequency(MONTH)
                .amountPaid(BigDecimal.valueOf(0.123f))
                .build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountPaid : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithLessThanMinimalDecimalValue() {
        //given
        Expense expense = Expense.builder()
                .type(MORTGAGE)
                .otherExpense("Other expense")
                .frequency(MONTH)
                .amountPaid(BigDecimal.valueOf(0))
                .build();
        //when
        Set<String> errors = validate(expense);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountPaid : must be greater than or equal to 0.01");
    }
}