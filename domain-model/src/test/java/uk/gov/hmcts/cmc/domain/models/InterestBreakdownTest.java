package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class InterestBreakdownTest {

    @Test
    public void shouldBeValidWhenCorrectDataIsProvided() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.valueOf(100.75),
            "It's like that because..."
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInvalidIfTotalAmountIsNull() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            null,
            "It's like that because..."
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).containsOnly("totalAmount : may not be null");
    }

    @Test
    public void shouldBeInvalidIfExplanationIsNull() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.TEN,
            null
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).containsOnly("explanation : may not be empty");
    }

    @Test
    public void shouldBeInvalidIfExplanationIsEmpty() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.TEN,
            ""
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).containsOnly("explanation : may not be empty");
    }

    @Test
    public void shouldBeInvalidWhenNegativeAmountIsProvided() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.valueOf(-1),
            "It's like that because..."
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).containsOnly("totalAmount : must be greater than or equal to 0.00");
    }

    @Test
    public void shouldBeValidWhenZeroIsProvidedForAmount() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.ZERO,
            "It's like that because..."
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    public void shouldBeInValidWhenTooManyFractionDigitsAreProvided() {
        InterestBreakdown interestBreakdown = new InterestBreakdown(
            BigDecimal.valueOf(123.456),
            "It's like that because..."
        );

        Set<String> validationErrors = validate(interestBreakdown);

        assertThat(validationErrors).containsOnly("totalAmount : can not be more than 2 fractions");
    }

}
