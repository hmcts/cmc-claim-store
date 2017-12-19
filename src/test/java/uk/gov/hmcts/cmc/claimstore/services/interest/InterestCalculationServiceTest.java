package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void shouldReturnValidValueRoundedTo2DecimalDigits() {
        BigDecimal claimAmount = new BigDecimal(8000L);
        BigDecimal interestRate = new BigDecimal(30L);
        BigDecimal result = service.calculateInterest(claimAmount, interestRate, START_DATE, START_DATE.plusDays(6));

        assertThat(result).isEqualTo(BigDecimal.valueOf(39.45).setScale(2, RoundingMode.HALF_UP));
    }
}
