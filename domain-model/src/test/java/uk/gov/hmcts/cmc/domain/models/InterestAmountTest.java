package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestAmountTest {

    @Test
    public void valueOfReturnObjectWithAmountEq0() {
        assertThat(new InterestAmount(BigDecimal.valueOf(0)).getAmount()).isEqualTo(expectedBigDecimal(0.00));
        assertThat(new InterestAmount(BigDecimal.valueOf(-0.004)).getAmount()).isEqualTo(expectedBigDecimal(0.00));
        assertThat(new InterestAmount(BigDecimal.valueOf(0.004)).getAmount()).isEqualTo(expectedBigDecimal(0.00));
    }

    @Test
    public void valueOfReturnObjectWithValidDoubleRoundedTo2DecimalDigits() {
        assertThat(new InterestAmount(BigDecimal.valueOf(1.123)).getAmount()).isEqualTo(expectedBigDecimal(1.12));
        assertThat(new InterestAmount(BigDecimal.valueOf(1.125)).getAmount()).isEqualTo(expectedBigDecimal(1.13));
        assertThat(new InterestAmount(BigDecimal.valueOf(1.0000000000009)).getAmount())
            .isEqualTo(expectedBigDecimal(1));
    }

    private static BigDecimal expectedBigDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
