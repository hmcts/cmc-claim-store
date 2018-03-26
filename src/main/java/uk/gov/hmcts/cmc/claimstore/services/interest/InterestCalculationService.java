package uk.gov.hmcts.cmc.claimstore.services.interest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.DIVISION_DECIMAL_SCALE;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.NUMBER_OF_DAYS_IN_YEAR;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.TO_FULL_PENNIES;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.asFraction;
import static uk.gov.hmcts.cmc.domain.amount.TotalAmountCalculator.calculateInterest;

@Service
public class InterestCalculationService {

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
        return calculateInterest(claimAmount, interestRate, fromDate, LocalDate.now(clock));
    }

    public BigDecimal calculateInterestUpToDate(
        BigDecimal claimAmount,
        BigDecimal interestRate,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        return calculateInterest(claimAmount, interestRate, fromDate, toDate);
    }
}
