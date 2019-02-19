package uk.gov.hmcts.cmc.domain.models;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.domain.BeanValidator.validate;

public class AmountBreakDownTest {

    @Test
    public void shouldBeSuccessfulValidationForFullAmountDetails() {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder().build();
        //when
        Set<String> validationMessages = validate(amountBreakDown);
        //then
        assertThat(validationMessages).isEmpty();
    }

    @Test
    public void shouldReturnValidationMessageWhenAmountBreakDownHasNullRows() {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder()
            .rows(null)
            .build();
        //when
        Set<String> validationMessages = validate(amountBreakDown);
        //then
        assertThat(validationMessages)
            .hasSize(1)
            .contains("rows : may not be null");
    }

    @Test
    public void shouldReturnValidationMessageWhenHasInvalidAmountRow() {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder()
            .rows(ImmutableList.of(
                AmountRow.builder().reason("reason").amount(null).build(),
                AmountRow.builder().reason("reason").amount(new BigDecimal("10")).build()))
            .build();
        //when
        Set<String> validationMessages = validate(amountBreakDown);
        //then
        assertThat(validationMessages)
            .hasSize(1)
            .contains("rows[0] : Claimant Amount is inValid");
    }

    @Test
    public void shouldReturnValidationMessageWhenBreakdownHasEmptyRowsList() {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder()
            .rows(ImmutableList.of())
            .build();
        //when
        Set<String> validationMessages = validate(amountBreakDown);
        //then
        assertThat(validationMessages)
            .hasSize(1)
            .contains("rows : Total value of at least 0.01 is required");
    }

    @Test
    public void shouldReturnValidationMessagesWhenBreakdownHasRowWith0Amount() {
        //given
        AmountBreakDown amountBreakDown = SampleAmountBreakdown.builder()
            .rows(ImmutableList.of(AmountRow.builder().reason("reason").amount(new BigDecimal("0")).build()))
            .build();
        //when
        Set<String> validationMessages = validate(amountBreakDown);
        //then
        assertThat(validationMessages)
            .hasSize(2)
            .contains("rows : Total value of at least 0.01 is required")
            .contains("rows[0].amount : must be greater than or equal to 0.01");
    }

}
