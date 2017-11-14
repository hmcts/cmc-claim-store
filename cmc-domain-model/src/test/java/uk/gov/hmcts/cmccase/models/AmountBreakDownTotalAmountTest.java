package uk.gov.hmcts.cmccase.models;

import org.junit.Test;
import uk.gov.hmcts.cmccase.models.amount.AmountBreakDown;

import java.math.BigDecimal;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class AmountBreakDownTotalAmountTest {

    @Test
    public void shouldCorrectlySumBreakdownAmounts() {
        AmountBreakDown breakDown = new AmountBreakDown(asList(
            new AmountRow("one", BigDecimal.ONE),
            new AmountRow("two", BigDecimal.TEN),
            new AmountRow("three", new BigDecimal("3.4"))
        ));

        assertThat(breakDown.getTotalAmount()).isEqualTo("14.4");
    }

    @Test
    public void shouldIgnoreRowsWithNullAmount() {
        AmountBreakDown breakDown = new AmountBreakDown(asList(
            new AmountRow("one", BigDecimal.ONE),
            new AmountRow("two", null),
            new AmountRow("three", new BigDecimal("1.2"))
        ));

        assertThat(breakDown.getTotalAmount()).isEqualTo("2.2");
    }

}
