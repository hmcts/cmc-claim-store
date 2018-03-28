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

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT;

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
        return Optional.ofNullable(calculateTotalAmount(claim, claim.getIssuedOn()));
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

        return calculateInterest(
            calculateDailyAmount(claimAmount, interestRate),
            daysBetween(fromDate, toDate)
        );
    }

    private static BigDecimal calculateInterest(Claim claim, LocalDate toDate) {
        ClaimData data = claim.getClaimData();
        BigDecimal interest;
        switch (data.getInterest().getType()) {
            case BREAKDOWN:
                interest = calculateBreakdownInterest(claim, toDate);
                break;
            case STANDARD:
            case DIFFERENT:
                interest = calculateFixedRateInterest(claim, toDate);
                break;
            case NO_INTEREST:
            default:
                interest = ZERO;
        }

        return interest;
    }

    private static BigDecimal calculateInterest(BigDecimal dailyAmount, BigDecimal numberOfDays) {
        return dailyAmount
            .multiply(numberOfDays)
            .setScale(TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateDailyAmount(BigDecimal claimAmount, BigDecimal interestRate) {
        return claimAmount
            .multiply(asFraction(interestRate))
            .divide(NUMBER_OF_DAYS_IN_YEAR, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateBreakdownInterest(Claim claim, LocalDate toDate) {
        Interest interest = claim.getClaimData().getInterest();
        InterestDate interestDate = claim.getClaimData().getInterestDate();
        BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        return calculateBreakdownInterest(interest, interestDate, claimAmount, claim.getIssuedOn(), toDate);
    }

    public static BigDecimal calculateBreakdownInterest(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn,
        LocalDate toDate
    ) {
        BigDecimal accruedInterest = ZERO;
        if (interestDate.getEndDateType() == SETTLED_OR_JUDGMENT) {
            Optional<BigDecimal> specificDailyAmount = interest.getSpecificDailyAmount();
            if (specificDailyAmount.isPresent()) {
                accruedInterest = calculateInterest(
                    specificDailyAmount.get(),
                    daysBetween(issuedOn, toDate)
                );
            } else {
                accruedInterest = calculateInterest(
                    calculateDailyAmount(claimAmount, interest.getRate()),
                    daysBetween(issuedOn, toDate)
                );
            }
        }
        BigDecimal interestValue = interest.getInterestBreakdown().getTotalAmount();
        return interestValue.add(accruedInterest);
    }

    public static BigDecimal asFraction(BigDecimal interestRate) {
        requireNonNegative(interestRate);
        return interestRate.divide(HUNDRED, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTotalAmount(Claim claim, LocalDate toDate) {
        ClaimData data = claim.getClaimData();

        if (data.getAmount() instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();
            BigDecimal feesPaid = data.getFeesPaidInPound();
            BigDecimal interest = calculateInterest(claim, toDate);

            return claimAmount.add(interest.add(feesPaid));
        }

        return null;
    }

    public static BigDecimal calculateInterestTillToday(Claim claim) {
        return calculateInterest(claim, LocalDate.now());
    }

    private static BigDecimal calculateFixedRateInterest(Claim claim, LocalDate toDate) {
        ClaimData data = claim.getClaimData();
        BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();
        BigDecimal rate = data.getInterest().getRate();
        LocalDate fromDate = getFromDate(claim);
        return calculateInterest(claimAmount, rate, fromDate, toDate);
    }

    private static LocalDate getFromDate(Claim claim) {
        return (claim.getClaimData().getInterestDate().getType() == InterestDate.InterestDateType.CUSTOM)
            ? claim.getClaimData().getInterestDate().getDate()
            : claim.getIssuedOn();
    }

    private static BigDecimal daysBetween(LocalDate startDate, LocalDate endDate) {
        // This should be enabled back and fixed properly
        // requireValidOrderOfDates(startDate, endDate);
        Duration duration = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());

        if (duration.isNegative()) {
            return ZERO;
        } else {
            return valueOf(duration.toDays());
        }
    }

    //    private static void requireValidOrderOfDates(LocalDate startDate, LocalDate endDate) {
    //        if (startDate.isAfter(endDate)) {
    //            throw new IllegalArgumentException(
    //                String.format("StartDate %s cannot be after endDate %s", startDate, endDate)
    //            );
    //        }
    //    }

    private static void requireNonNegative(BigDecimal value) {
        requireNonNull(value);
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Expected non-negative number");
        }
    }
}
