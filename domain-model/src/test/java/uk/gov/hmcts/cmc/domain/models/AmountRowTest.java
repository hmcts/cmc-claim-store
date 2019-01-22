package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class AmountRowTest {

    @Test
    public void shouldBeSuccessfulValidationForFullAmountDetails() {
        //given
        AmountRow amountRow = AmountRow.builder().reason("reason").amount(new BigDecimal("40")).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountHasMissingReason() {
        //given
        AmountRow amountRow = AmountRow.builder().reason(null).amount(new BigDecimal("40")).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("Claimant Amount is inValid");
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountHasMissingAmount() {
        //given
        AmountRow amountRow = AmountRow.builder().reason("reason").amount(null).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("Claimant Amount is inValid");
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountHasValueLessThanMinimum() {
        //given
        AmountRow amountRow = AmountRow.builder().reason("reason").amount(new BigDecimal("0.00")).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : must be greater than or equal to 0.01");
    }

    @Test
    public void shouldBeSuccessfulValidationForFullAmountDetailsWithTwoFraction() {
        //given
        AmountRow amountRow = AmountRow.builder().reason("reason").amount(new BigDecimal("40.50")).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountHasValueWithMoreThanAllowedFractions() {
        //given
        AmountRow amountRow = AmountRow.builder().reason("reason").amount(new BigDecimal("40.123")).build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors)
            .hasSize(1)
            .contains("amount : can not be more than 2 fractions");
    }
}
