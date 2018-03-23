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
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import static java.math.BigDecimal.valueOf;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.cmc.domain.models.InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT;

public class TotalAmountCalculator {

    public static final int TO_FULL_PENNIES = 2;
    public static final BigDecimal NUMBER_OF_DAYS_IN_YEAR = new BigDecimal(365L);
    public static final int DIVISION_DECIMAL_SCALE = 10;
    private static final BigDecimal HUNDRED = valueOf(100);

    private static final Comparator<LocalDate> LOCAL_DATE_COMPARATOR = Comparator.comparing(LocalDate::toEpochDay);

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

    private static BigDecimal calculateDailyAmount(BigDecimal claimAmount, BigDecimal interestRate) {
        return claimAmount
            .multiply(asFraction(interestRate))
            .divide(NUMBER_OF_DAYS_IN_YEAR, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateInterest(BigDecimal dailyAmount, BigDecimal numberOfDays) {
        return dailyAmount
            .multiply(numberOfDays)
            .setScale(TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateBreakdownInterest(Claim claim, LocalDate toDate) {
        Interest interest = claim.getClaimData().getInterest();
        InterestDate interestDate = claim.getClaimData().getInterestDate();
        BigDecimal accruedInterest = BigDecimal.ZERO;
        if (interestDate.getEndDateType() == SETTLED_OR_JUDGMENT) {
            if (interest.getSpecificDailyAmount().isPresent()) {
                accruedInterest = calculateInterest(
                    interest.getSpecificDailyAmount().get(),
                    daysBetween(claim.getIssuedOn(), toDate)
                );
            } else {
                BigDecimal claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
                accruedInterest = calculateInterest(
                    calculateDailyAmount(claimAmount, interest.getRate()),
                    daysBetween(claim.getIssuedOn(), toDate)
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
            BigDecimal interest = BigDecimal.ZERO;
            BigDecimal feesPaid = data.getFeesPaidInPound();

            if (data.getInterest().getType() == Interest.InterestType.BREAKDOWN) {
                interest = calculateBreakdownInterest(claim, toDate);
            } else if (data.getInterest().getType() != Interest.InterestType.NO_INTEREST) {
                interest = calculateFixedRateInterest(claim, toDate);
            }

            return claimAmount.add(interest.add(feesPaid));
        }

        return null;
    }

    private static BigDecimal calculateFixedRateInterest(Claim claim, LocalDate toDate) {
        ClaimData data = claim.getClaimData();
        BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();
        BigDecimal rate = data.getInterest().getRate();
        LocalDate fromDate = getFromDate(claim);
        return calculateInterest(claimAmount, rate, fromDate, getLatestDate(toDate, claim.getIssuedOn()));
    }

    private static LocalDate getLatestDate(LocalDate firstDate, LocalDate secondDate) {
        return Stream.of(firstDate, secondDate).max(LOCAL_DATE_COMPARATOR)
            .orElseThrow(() -> new IllegalArgumentException("One of the dates is not correct"));
    }

    private static LocalDate getFromDate(Claim claim) {
        return (claim.getClaimData().getInterestDate().getType() == InterestDate.InterestDateType.SUBMISSION)
            ? claim.getIssuedOn()
            : claim.getClaimData().getInterestDate().getDate();
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
            throw new IllegalArgumentException(
                String.format("StartDate %s cannot be after endDate %s", startDate, endDate)
            );
        }
    }

    private static void requireNonNegative(BigDecimal value) {
        requireNonNull(value);
        if (value.signum() == -1) {
            throw new IllegalArgumentException("Expected non-negative number");
        }
    }
}
