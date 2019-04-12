package uk.gov.hmcts.cmc.domain.utils;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class MonetaryConversionsTest {

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullAmount() {
        MonetaryConversions.penniesToPounds(null);
    }

    @Test
    public void zeroPenniesShouldConvertToZeroPounds() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(BigDecimal.ZERO);
        assertThat(converted).isEqualByComparingTo("0");
    }

    @Test
    public void zeroPoundsShouldConvertToZeroPennies() {
        BigInteger converted = MonetaryConversions.poundsToPennies(BigDecimal.ZERO);
        assertThat(converted).isEqualTo("0");
    }

    @Test
    public void onePennyShouldConvertToOneHundredthOfPound() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("1"));
        assertThat(converted).isEqualByComparingTo("0.01");
    }

    @Test
    public void oneHundredthOfPoundShouldConvertToOnePenny() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("0.01"));
        assertThat(converted).isEqualTo("1");
    }

    @Test
    public void tenPenniesShouldConvertToOneTenthOfPound() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("10"));
        assertThat(converted).isEqualByComparingTo("0.10");
    }

    @Test
    public void oneTenthofPoundShouldConvertToTenPennies() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("0.10"));
        assertThat(converted).isEqualTo("10");
    }

    @Test
    public void hundredPenniesShouldConvertToOnePound() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("100"));
        assertThat(converted).isEqualByComparingTo("1.00");
    }

    @Test
    public void onePoundShouldConvertToHundredPennies() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("1.00"));
        assertThat(converted).isEqualTo("100");
    }

    @Test
    public void twoAndHalfThousandPenniesShouldConvertToTwentyFivePounds() {
        BigDecimal converted = MonetaryConversions.penniesToPounds(new BigDecimal("2500"));
        assertThat(converted).isEqualByComparingTo("25.00");
    }

    @Test
    public void twentyFivePoundsShouldConvertTotwoAndHalfThousandPennies() {
        BigInteger converted = MonetaryConversions.poundsToPennies(new BigDecimal("25.00"));
        assertThat(converted).isEqualTo("2500");
    }

}
