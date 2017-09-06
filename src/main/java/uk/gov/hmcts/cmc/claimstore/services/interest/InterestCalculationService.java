package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;

import static java.math.BigDecimal.valueOf;
import static java.util.Objects.requireNonNull;

@Service
public class InterestCalculationService {

    private static final int DIVISION_DECIMAL_SCALE = 10;
    private static final int TO_FULL_PENNIES = 2;

    private static final BigDecimal HUNDRED = valueOf(100);
    private static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal("365.242199");

    private final Clock clock;

    @Autowired
    public InterestCalculationService(Clock clock) {
        this.clock = clock;
    }

    public BigDecimal calculateDailyAmountFor(BigDecimal claimAmount, BigDecimal interestRate) {
        requireNonNull(claimAmount);
        requireNonNull(interestRate);
        return claimAmount
            .multiply(asFraction(interestRate))
            .divide(NUMBER_OF_DAYS_IN_YEAR, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP)
            .setScale(TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateInterestUpToNow(BigDecimal claimAmount, BigDecimal interestRate, LocalDate fromDate) {
        requireNonNull(claimAmount);
        requireNonNull(interestRate);
        requireNonNull(fromDate);

        return claimAmount
            .multiply(asFraction(interestRate))
            .multiply(daysUpToNowSince(fromDate))
            .divide(NUMBER_OF_DAYS_IN_YEAR, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP)
            .setScale(TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }

    private BigDecimal asFraction(BigDecimal interestRate) {
        return interestRate.divide(HUNDRED, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal daysUpToNowSince(LocalDate interestDate) {
        return valueOf(Duration.between(
                interestDate.atStartOfDay(),
                LocalDate.now(clock).atStartOfDay()
            ).toDays());
    }

}
