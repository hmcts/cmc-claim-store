package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InterestCalculationServiceDailyAmountCalculationTest {

    private static final BigDecimal HUNDRED_PERCENT_INTEREST = valueOf(100);
    private static final BigDecimal EIGHT_PERCENT_INTEREST = valueOf(8);

    @Test
    public void shouldThrowNullPointerWhenGivenNullAmount() {
        assertThrows(NullPointerException.class, () -> {
            InterestCalculationService service = new InterestCalculationService(Clock.systemDefaultZone());
            service.calculateDailyAmountFor(null, HUNDRED_PERCENT_INTEREST);
        });
    }

    @Test
    public void shouldThrowNullPointerWhenGivenNullInterest() {
        assertThrows(NullPointerException.class, () -> {
            InterestCalculationService service = new InterestCalculationService(Clock.systemDefaultZone());
            service.calculateDailyAmountFor(BigDecimal.TEN, null);
        });
    }

    @Test
    public void dailyInterestAmountShouldEqualTwoPenniesFor100PoundsAnd8Percent() {
        InterestCalculationService service = new InterestCalculationService(Clock.systemDefaultZone());

        BigDecimal dailyAmount = service.calculateDailyAmountFor(
            new BigDecimal("100.00"), EIGHT_PERCENT_INTEREST
        );

        assertThat(dailyAmount).isEqualTo("0.02");
    }

}
