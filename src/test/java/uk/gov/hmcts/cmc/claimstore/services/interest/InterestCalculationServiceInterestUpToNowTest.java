package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

public class InterestCalculationServiceInterestUpToNowTest {

    private static final BigDecimal HUNDRED_POUNDS = valueOf(100);
    private static final BigDecimal THIRTEEN_PERCENT_INTEREST = valueOf(13);
    private static final LocalDate TWENTY_DAYS_AGO = LocalDate.now().minusDays(20);

    private static final BigDecimal MORE_POUNDS = valueOf(12345);
    private static final BigDecimal SEVENTY_FIVE_PERCENT_INTEREST = valueOf(75);
    private static final LocalDate FIVE_HUNDRED_DAYS_AGO = LocalDate.now().minusDays(500);

    private static final BigDecimal ONE_PENNY = new BigDecimal("0.01");
    private static final BigDecimal EIGHT_PERCENT = valueOf(8);
    private static final LocalDate TEN_K_DAYS_AGO = LocalDate.now().minusDays(10000);

    private final InterestCalculationService service = new InterestCalculationService(Clock.systemDefaultZone());

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullAmount() {
        service.calculateInterestUpToNow(null, THIRTEEN_PERCENT_INTEREST, TWENTY_DAYS_AGO);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullRate() {
        service.calculateInterestUpToNow(HUNDRED_POUNDS, null, TWENTY_DAYS_AGO);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenGivenNullInterestDate() {
        service.calculateInterestUpToNow(HUNDRED_POUNDS, THIRTEEN_PERCENT_INTEREST, null);
    }

    @Test
    public void interestUpToNowFrom100PoundsOf33PercentFor20DaysShouldEqualExpectedValue() {
        BigDecimal calculated = service.calculateInterestUpToNow(
            HUNDRED_POUNDS, THIRTEEN_PERCENT_INTEREST, TWENTY_DAYS_AGO
        );
        assertThat(calculated).isEqualByComparingTo("0.71");
    }

    @Test
    public void interestUpToNowFrom12345PoundsOf75PercentFor500DaysShouldEqualExpectedValue() {
        BigDecimal calculated = service.calculateInterestUpToNow(
            MORE_POUNDS, SEVENTY_FIVE_PERCENT_INTEREST, FIVE_HUNDRED_DAYS_AGO
        );
        assertThat(calculated).isEqualByComparingTo("12683.22");
    }

    @Test
    public void interestUpToNowFrom1PennyOf8PercentFor10000DaysShouldEqualExpectedValue() {
        BigDecimal calculated = service.calculateInterestUpToNow(
            ONE_PENNY, EIGHT_PERCENT, TEN_K_DAYS_AGO
        );
        assertThat(calculated).isEqualByComparingTo("0.02");
    }

}
