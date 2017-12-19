package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.junit.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

public class InterestCalculationServiceTest {

    private static final LocalDate START_DATE = LocalDate.now();
    private static final BigDecimal NEGATIVE = BigDecimal.valueOf(-12);

    private InterestCalculationService service = new InterestCalculationService(Clock.systemDefaultZone());

    @Test(expected = IllegalArgumentException.class)
    public void shouldIllegalArgumentExceptionWhenAmountIsNegative() {
        service.calculateInterest(NEGATIVE, BigDecimal.ONE, START_DATE, START_DATE.plusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldIllegalArgumentExceptionWhenInterestRateIsNegative() {
        service.calculateInterest(BigDecimal.ONE, NEGATIVE, START_DATE, START_DATE.plusDays(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldIllegalArgumentExceptionWhenStartDateIsAfterEndDate() {
        service.calculateInterest(BigDecimal.ONE, BigDecimal.ONE, START_DATE, START_DATE.minusDays(1));
    }
}
