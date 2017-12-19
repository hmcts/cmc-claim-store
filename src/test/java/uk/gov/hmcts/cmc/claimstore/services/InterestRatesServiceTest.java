package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InterestRatesServiceTest {

    private static final LocalDate VALID_FROM = LocalDate.now();
    private static final LocalDate VALID_TO = VALID_FROM.plusDays(10);
    private static final BigDecimal ZERO = BigDecimal.valueOf(0).setScale(2, RoundingMode.HALF_UP);

    private InterestRateService interestRateService;

    @Before
    public void setUp() {
        interestRateService = new InterestRateService();
    }

    @Test
    public void calculateRateShouldReturn0WhenRateIs0() {
        final BigDecimal amount = BigDecimal.ONE;
        assertThat(interestRateService.calculateRate(VALID_FROM, VALID_TO, BigDecimal.ZERO, amount)).isEqualTo(ZERO);
    }

    @Test
    public void calculateRateShouldReturn0WhenAmountIs0() {
        final BigDecimal rate = BigDecimal.ONE;
        assertThat(interestRateService.calculateRate(VALID_FROM, VALID_TO, rate, BigDecimal.ZERO))
            .isEqualTo(ZERO);
    }

    @Test
    public void calculateRateShouldReturn0WhenNumberOfDaysIs0() {
        final LocalDate now = LocalDate.now();
        assertThat(interestRateService.calculateRate(now, now, BigDecimal.ONE, BigDecimal.ONE)).isEqualTo(ZERO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenDateFromIsNull() {
        interestRateService.calculateRate(null, VALID_TO, BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenDateToIsNull() {
        interestRateService.calculateRate(VALID_FROM, null, BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenDateToIsBeforeDateFrom() {
        final LocalDate now = LocalDate.now();
        interestRateService.calculateRate(now.plusDays(1), now.minusDays(1), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenRateIsNegative() {
        interestRateService.calculateRate(VALID_FROM, VALID_TO, BigDecimal.valueOf(-1), BigDecimal.ONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenAmountIsNegative() {
        interestRateService.calculateRate(VALID_FROM, VALID_TO, BigDecimal.ONE, BigDecimal.valueOf(-1));
    }

    @Test
    public void calculateRateShouldReturnValue() {
        LocalDate today = LocalDate.now();

        assertThat(
            interestRateService.calculateRate(
                today,
                today.plusDays(6),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(8000)
            ).setScale(2, RoundingMode.HALF_UP)
        ).isEqualTo(BigDecimal.valueOf(39.45));
    }
}
