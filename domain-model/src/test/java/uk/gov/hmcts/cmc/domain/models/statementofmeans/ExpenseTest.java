package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.MORTGAGE;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Expense.ExpenseType.OTHER;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

public class ExpenseTest {
    public static Expense.ExpenseBuilder newSampleOfExpenseBuilder() {
        return Expense.builder()
                .type(MORTGAGE)
                .frequency(MONTH)
                .amountPaid(BigDecimal.valueOf(10));
    }

    @Test
    public void shouldBeSuccessfulValidationForExpense() {
        //given
        Expense expense = newSampleOfExpenseBuilder().build();
        //when
        Set<String> response = validate(expense);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeSuccessfulValidationForOtherExpense() {
        //given
        Expense expense = Expense.builder()
            .type(OTHER)
            .otherExpense("My other expense")
            .frequency(MONTH)
            .amountPaid(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(expense);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForOtherExpenseWhenNoDescriptionGiven() {
        //given
        Expense expense = Expense.builder()
            .type(OTHER)
            .frequency(MONTH)
            .amountPaid(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(expense);
        //then
        assertThat(response).hasSize(1).contains("otherExpense : may not be null when type is 'Other'");
    }

    @Test
    public void shouldBeInvalidForOtherExpensePopulatedWhenTypeIsNotOther() {
        //given
        Expense expense = Expense.builder()
            .type(MORTGAGE)
            .otherExpense("This shouldn't be populated")
            .frequency(MONTH)
            .amountPaid(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(expense);
        //then
        assertThat(response).hasSize(1)
            .contains("otherExpense : may not be provided when type is 'Mortgage'");
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
