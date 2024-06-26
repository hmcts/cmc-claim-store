package uk.gov.hmcts.cmc.domain.models;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cmc.domain.models.amount.AmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;

import java.util.Set;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class AmountRangeTest {
    @Test
    public void shouldBeSuccessfulValidationForValidAmountDetails() {
        //given
        AmountRange amountRow = SampleAmountRange.builder().build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForMaximumValue() {
        //given
        AmountRange amountRow = SampleAmountRange.builder()
            .higherValue(valueOf(9999999.99))
            .lowerValue(valueOf(9999999.99))
            .build();

        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldBeSuccessfulValidationForMinimum() {
        //given
        AmountRange amountRow = SampleAmountRange.builder()
            .higherValue(valueOf(0.01))
            .lowerValue(valueOf(0.01))
            .build();

        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldHaveErrorsForValueHigherThanMaximum() {
        //given
        AmountRange amountRow = SampleAmountRange.builder()
            .higherValue(valueOf(10000000))
            .build();
        //when
        Set<String> errors = validate(amountRow);
        //then
        assertThat(errors).containsExactlyInAnyOrder("higherValue : can not be more than 2 fractions");
    }

    @Test
    public void shouldHaveErrorsForValueLowerThanMinimum() {
        //given
        AmountRange amountRow = SampleAmountRange.builder()
            .higherValue(valueOf(0))
            .lowerValue(valueOf(0))
            .build();

        //when
        Set<String> errors = validate(amountRow);
        //then

        String[] expectedErroMessages = {"higherValue : must be greater than or equal to 0.01",
            "lowerValue : must be greater than or equal to 0.01"};

        assertThat(errors).containsExactlyInAnyOrder(expectedErroMessages);
    }
}
