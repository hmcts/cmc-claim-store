package uk.gov.hmcts.cmc.claimstore.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class InterestRatesServiceTest {

    private static final LocalDate VALID_FROM = LocalDate.now();
    private static final LocalDate VALID_TO = VALID_FROM.plusDays(10);

    private InterestRateService interestRateService;

    @Before
    public void setUp() {
        interestRateService = new InterestRateService();
    }

    @Test
    public void calculateRateShouldReturn0WhenRateIs0() {
        final double amount = 1;
        assertThat(interestRateService.calculateRate(VALID_FROM, VALID_TO, 0, amount).getAmount()).isEqualTo(0);
    }

    @Test
    public void calculateRateShouldReturn0WhenAmountIs0() {
        final double rate = 1;
        assertThat(interestRateService.calculateRate(VALID_FROM, VALID_TO, rate, 0).getAmount()).isEqualTo(0);
    }

    @Test
    public void calculateRateShouldReturn0WhenNumberOfDaysIs0() {
        final LocalDate now = LocalDate.now();
        assertThat(interestRateService.calculateRate(now, now, 1, 1).getAmount()).isEqualTo(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenDateFromIsNull() {
        interestRateService.calculateRate(null, VALID_TO, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenDateToIsNull() {
        interestRateService.calculateRate(VALID_FROM, null, 1, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void calculateRateShouldThrowExceptionWhenDateToIsBeforeDateFrom() {
        final LocalDate now = LocalDate.now();
        interestRateService.calculateRate(now.plusDays(1), now.minusDays(1), 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenRateIsNegative() {
        interestRateService.calculateRate(VALID_FROM, VALID_TO, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateRateShouldThrowExceptionWhenAmountIsNegative() {
        interestRateService.calculateRate(VALID_FROM, VALID_TO, 1, -1);
    }

    @Test
    public void calculateRateShouldReturnValue() {
        LocalDate today = LocalDate.now();
        assertThat(
            interestRateService.calculateRate(today, today.plusDays(6), 30, 8000).getAmount()
        ).isEqualTo(39.45);
    }
}

