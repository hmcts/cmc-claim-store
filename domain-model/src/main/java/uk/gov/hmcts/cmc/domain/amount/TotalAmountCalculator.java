package uk.gov.hmcts.cmc.domain.amount;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static java.math.BigDecimal.valueOf;
import static java.util.Objects.requireNonNull;

public class TotalAmountCalculator {

    public static final int TO_FULL_PENNIES = 2;
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);
    public static final int DIVISION_DECIMAL_SCALE = 10;
    private static final BigDecimal HUNDRED = valueOf(100);

    private TotalAmountCalculator() {
        // do not instantiate
    }

    public static Optional<BigDecimal> totalTillToday(Claim claim) {
        return Optional.ofNullable(calculateTotalAmount(claim, LocalDate.now()));
    }

    public static Optional<BigDecimal> totalTillDateOfIssue(Claim claim) {
        return Optional.ofNullable(calculateTotalAmount(claim, claim.getCreatedAt().toLocalDate()));
    }

    public static BigDecimal calculateInterest(
        BigDecimal claimAmount,
        BigDecimal interestRate,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        requireNonNegative(claimAmount);
        requireNonNegative(interestRate);
        requireNonNull(fromDate);
        requireNonNull(toDate);

        return claimAmount
            .multiply(asFraction(interestRate))
            .multiply(daysBetween(fromDate, toDate))
            .divide(NUMBER_OF_DAYS_IN_YEAR, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP)
            .setScale(TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }

    public static BigDecimal asFraction(BigDecimal interestRate) {
        requireNonNegative(interestRate);
        return interestRate.divide(HUNDRED, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTotalAmount(Claim claim, LocalDate toDate) {
        ClaimData data = claim.getClaimData();

        if (data.getAmount() instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();
            BigDecimal rate = data.getInterest().getRate();

            if (data.getInterest().getType() != Interest.InterestType.NO_INTEREST) {
            LocalDate fromDate = (data.getInterestDate().getType() == InterestDate.InterestDateType.SUBMISSION)
                ? claim.getCreatedAt().toLocalDate()
                : data.getInterestDate().getDate();
                return claimAmount
                    .add(data.getFeesPaidInPound())
                    .add(calculateInterest(claimAmount, rate, fromDate, toDate));
            }

            return claimAmount.add(data.getFeesPaidInPound());
        }

        return null;
    }


    private static BigDecimal daysBetween(LocalDate startDate, LocalDate endDate) {
        requireValidOrderOfDates(startDate, endDate);
        return valueOf(Duration.between(
            startDate.atStartOfDay(),
            endDate.atStartOfDay()
        ).toDays());
    }

    private static void requireValidOrderOfDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("StartDate cannot be after endDate");
        }
    }

    private static void requireNonNegative(BigDecimal value) {
        requireNonNull(value);
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Expected non-negative number");
        }
    }
}
