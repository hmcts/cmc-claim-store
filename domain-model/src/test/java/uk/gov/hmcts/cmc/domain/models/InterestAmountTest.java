package uk.gov.hmcts.cmc.domain.models;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InterestAmountTest {

    @Test
    public void valueOfReturnObjectWithAmountEq0() {
        assertThat(InterestAmount.valueOf(0).getAmount()).isEqualTo(0);
        assertThat(InterestAmount.valueOf(-0.004).getAmount()).isEqualTo(0);
        assertThat(InterestAmount.valueOf(0.004).getAmount()).isEqualTo(0);
    }

    @Test
    public void valueOfReturnObjectWithValidDoubleRoundedTo2DecimalDigits() {
        assertThat(InterestAmount.valueOf(1.123).getAmount()).isEqualTo(1.12);
        assertThat(InterestAmount.valueOf(1.125).getAmount()).isEqualTo(1.13);
        assertThat(InterestAmount.valueOf(1.0000000000009).getAmount()).isEqualTo(1.00);
    }
}
