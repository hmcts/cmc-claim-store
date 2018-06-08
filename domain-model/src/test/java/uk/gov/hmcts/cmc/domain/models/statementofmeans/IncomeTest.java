package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.Income.IncomeType.JOB;
import static uk.gov.hmcts.cmc.domain.models.statementofmeans.PaymentFrequency.MONTH;

public class IncomeTest {

    @Test
    public void shouldBeSuccessfulValidationForIncome() {
        //given
        Income income = Income.builder()
                .type(JOB)
                .otherSource("Other source")
                .frequency(MONTH)
                .amountReceived(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> response = validate(income);
        //then
        assertThat(response).hasSize(0);
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
                .otherSource("Other source")
                .frequency(MONTH)
                .amountReceived(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("type : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullPaymentFrequency() {
        //given
        Income income = Income.builder()
                .type(JOB)
                .otherSource("Other source")
                .amountReceived(BigDecimal.valueOf(10))
                .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("frequency : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullAmountReceived() {
        //given
        Income income = Income.builder()
                .type(JOB)
                .otherSource("Other source")
                .frequency(MONTH)
                .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountReceived : may not be null");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithMoreThanTwoFractions() {
        //given
        Income income = Income.builder()
                .type(JOB)
                .otherSource("Other source")
                .frequency(MONTH)
                .amountReceived(BigDecimal.valueOf(0.123f))
                .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountReceived : can not be more than 2 fractions");
    }

    @Test
    public void shouldBeInvalidForAmountReceivedWithLessThanMinimalDecimalValue() {
        //given
        Income income = Income.builder()
                .type(JOB)
                .otherSource("Other source")
                .frequency(MONTH)
                .amountReceived(BigDecimal.valueOf(0))
                .build();
        //when
        Set<String> errors = validate(income);
        //then
        assertThat(errors)
                .hasSize(1)
                .contains("amountReceived : must be greater than or equal to 0.01");
    }
}