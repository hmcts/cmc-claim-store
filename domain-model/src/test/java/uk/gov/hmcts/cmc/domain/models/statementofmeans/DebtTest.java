package uk.gov.hmcts.cmc.domain.models.statementofmeans;

import org.junit.Test;

import java.util.Set;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class DebtTest {

    @Test
    public void shouldBeSuccessfulValidationForCorrectDebt() {
        //given
        Debt debt = Debt.builder()
            .description("My debt")
            .monthlyPayments(ONE)
            .totalOwed(TEN)
            .build();
        //when
        Set<String> response = validate(debt);
        //then
        assertThat(response).hasSize(0);
    }

    @Test
    public void shouldBeInvalidForAllNullFields() {
        //given
        Debt debt = Debt.builder().build();
        //when
        Set<String> errors = validate(debt);
        //then
        assertThat(errors)
            .hasSize(3);
    }

    @Test
    public void shouldBeInvalidForNullDescription() {
        //given
        Debt debt = Debt.builder()
            .monthlyPayments(ONE)
            .totalOwed(TEN)
            .build();
        //when
        Set<String> errors = validate(debt);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("description : may not be empty");
    }

    @Test
    public void shouldBeInvalidForNullTotalOwed() {
        //given
        Debt debt = Debt.builder()
            .description("My debt")
            .monthlyPayments(ONE)
            .build();
        //when
        Set<String> errors = validate(debt);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("totalOwed : may not be null");
    }

    @Test
    public void shouldBeInvalidForNullMonthlyPayments() {
        //given
        Debt debt = Debt.builder()
            .description("My debt")
            .totalOwed(TEN)
            .build();
        //when
        Set<String> errors = validate(debt);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("monthlyPayments : may not be null");
    }
}
