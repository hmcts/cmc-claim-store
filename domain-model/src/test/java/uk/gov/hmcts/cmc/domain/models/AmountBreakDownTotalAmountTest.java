package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AmountBreakDownTotalAmountTest {

    @Test
    public void shouldCorrectlySumBreakdownAmounts() {
        AmountBreakDown breakDown = new AmountBreakDown(asList(
            AmountRow.builder().reason("one").amount(BigDecimal.ONE).build(),
            AmountRow.builder().reason("two").amount(BigDecimal.TEN).build(),
            AmountRow.builder().reason("three").amount(new BigDecimal("3.4")).build()
        ));

        assertThat(breakDown.getTotalAmount()).isEqualTo("14.4");
    }

    @Test
    public void shouldIgnoreRowsWithNullAmount() {
        AmountBreakDown breakDown = new AmountBreakDown(asList(
            AmountRow.builder().reason("one").amount(BigDecimal.ONE).build(),
            AmountRow.builder().reason("two").amount(null).build(),
            AmountRow.builder().reason("three").amount(new BigDecimal("1.2")).build()
        ));

        assertThat(breakDown.getTotalAmount()).isEqualTo("2.2");
    }

}
