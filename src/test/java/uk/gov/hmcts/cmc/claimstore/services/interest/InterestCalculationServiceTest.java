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
    public void shouldThrowIllegalArgumentExceptionWhenAmountIsNegative() {
        service.calculateDailyAmountFor(NEGATIVE, NEGATIVE);
    }
}
