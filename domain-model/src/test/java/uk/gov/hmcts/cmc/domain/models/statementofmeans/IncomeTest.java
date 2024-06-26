package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.JOB;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.OTHER;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

public class IncomeTest {
    public static Income.IncomeBuilder newSampleOfIncomeBuilder() {
        return Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(10));
    }

    @Test
    public void shouldBeSuccessfulValidationForPositiveIncome() {
        //given
        Income income = newSampleOfIncomeBuilder().build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForZeroIncome() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(0)).build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForOtherSourceOfIncome() {
        //given
        Income income = Income.builder()
            .type(OTHER)
            .otherSource("Other source of income")
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response).isEmpty();
    }

    @Test
    public void shouldBeInvalidForOtherSourceOfIncomeWhenNoDescriptionGiven() {
        //given
        Income income = Income.builder()
            .type(OTHER)
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("otherSource : may not be null when type is 'Other'");
    }

    @Test
    public void shouldBeInvalidForOtherSourcePopulatedWhenTypeIsNotOther() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .otherSource("This shouldn't be populated")
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response)
            .hasSize(1)
            .contains("otherSource : may not be provided when type is 'Income from your job'");
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        Income income = Income.builder().build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullType() {
        //given
        Income income = Income.builder()
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("type : must not be null");
    }

    @Test
    public void shouldBeInvalidForNullPaymentFrequency() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .amount(BigDecimal.valueOf(10))
            .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("frequency : must not be null");
    }

    @Test
    public void shouldBeInvalidForNullAmountReceived() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : must not be null");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithMoreThanTwoFractions() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(0.123f))
            .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithLessThanMinimalDecimalValue() {
        //given
        Income income = Income.builder()
            .type(JOB)
            .frequency(MONTH)
            .amount(BigDecimal.valueOf(-0.01))
            .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : must be greater than or equal to 0.00");
    }
}
