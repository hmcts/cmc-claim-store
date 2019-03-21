package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

public class FormattingTest {

    @Test
    public void formatDateShouldFormatWithExpectedPattern() {
        LocalDate date = LocalDate.of(2017, 7, 27);

        String formattedDate = Formatting.formatDate(date);

        assertThat(formattedDate).isEqualTo("27 July 2017");
    }

    @Test
    public void formatDateShouldFormatWithExpectedPatternForOneDigitDay() {
        LocalDate date = LocalDate.of(2017, 7, 1);

        String formattedDate = Formatting.formatDate(date);

        assertThat(formattedDate).isEqualTo("1 July 2017");
    }

    @Test(expected = NullPointerException.class)
    public void formatDateShouldThrowNullPointerWhenGivenNullDate() {
        Formatting.formatDate((LocalDate) null);
    }

    @Test(expected = NullPointerException.class)
    public void formatDateShouldThrowNullPointerWhenGivenNullDateTime() {
        Formatting.formatDate((LocalDateTime) null);
    }

    @Test
    public void formatDateTimeShouldFormatWithExpectedPattern() {
        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 27, 17, 44);

        String formattedDate = Formatting.formatDateTime(dateTime);

        assertThat(formattedDate).matches("27 July 2017 at 5:44(?i)pm");
    }

    @Test(expected = NullPointerException.class)
    public void formatDateTimeShouldThrowNullPointerWhenGivenNullDateTime() {
        Formatting.formatDateTime(null);
    }

    @Test
    public void formatMoneyShouldFormatWithExpectedPattern() {
        BigDecimal value = new BigDecimal("123456.78");

        String formatted = Formatting.formatMoney(value);

        assertThat(formatted).isEqualTo("£123,456.78");
    }

    @Test
    public void formatMoneyShouldFormatZeroesWithExpectedPattern() {
        BigDecimal value = new BigDecimal("123456.00");

        String formatted = Formatting.formatMoney(value);

        assertThat(formatted).isEqualTo("£123,456");
    }

    @Test(expected = NullPointerException.class)
    public void formatMoneyShouldThrowNullPointerWhenGivenNullAmount() {
        BigInteger value = null;
        Formatting.formatMoney(value);
    }

    @Test
    public void formatDateShouldFormatDateOnlyIfGivenDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2017, 7, 27, 17, 44);

        String formattedDate = Formatting.formatDate(dateTime);

        assertThat(formattedDate).isEqualTo("27 July 2017");
    }

    @Test(expected = NullPointerException.class)
    public void formatPercentShouldThrowNullPointerForNullArgument() {
        Formatting.formatPercent(null);
    }

    @Test
    public void formatPercentShouldCorrectlyFormatIntegralPercent() {
        String formatted = Formatting.formatPercent(valueOf(8));

        assertThat(formatted).isEqualTo("8%");
    }

    @Test
    public void formatPercentShouldCorrectlyFormatDecimalPercent() {
        String formatted = Formatting.formatPercent(new BigDecimal("4.5"));

        assertThat(formatted).isEqualTo("4.5%");
    }
}
