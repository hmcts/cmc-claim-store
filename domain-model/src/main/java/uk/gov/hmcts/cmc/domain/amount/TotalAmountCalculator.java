package uk.gov.hmcts.cmc.domain.amount;

import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.ClaimData;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.Amount;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;
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

    public static Optional<BigDecimal> totalClaimAmount(Claim claim) {
        return Optional.ofNullable(claim.getClaimData().getAmount())
            .filter(AmountBreakDown.class::isInstance)
            .map(AmountBreakDown.class::cast)
            .map(AmountBreakDown::getTotalAmount);
    }


    public static Optional<BigDecimal> amountWithInterestUntilIssueDate(Amount amount, Interest interest, LocalDate issuedOn) {
        return Optional.ofNullable(calculateTotalAmount(amount, interest, LocalDate.now(), issuedOn));
    }

    private static Optional<BigDecimal> calculateInterest(Amount amount, Interest interest, LocalDate toDate, LocalDate issuedOn) {
        if (interest == null) {
            return Optional.empty();
        }

        if (interest.getType() == Interest.InterestType.BREAKDOWN) {
            return Optional.ofNullable(calculateBreakdownInterest(amount, interest, toDate, issuedOn));
        } else if (interest.getType() != Interest.InterestType.NO_INTEREST) {
            return Optional.ofNullable(calculateFixedRateInterest(amount, interest, toDate, issuedOn));
        }

        return Optional.of(ZERO);
    }

    private static BigDecimal calculateBreakdownInterest(Amount amount, Interest interest, LocalDate toDate, LocalDate issuedOn) {
        InterestDate interestDate = interest.getInterestDate();
        if (amount instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) amount).getTotalAmount();
            return calculateBreakdownInterest(interest, interestDate, claimAmount, issuedOn, toDate);
        }

        return ZERO;
    }

    private static BigDecimal calculateFixedRateInterest(Amount amount, Interest interest, LocalDate toDate, LocalDate issuedOn) {
        if (amount instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) amount).getTotalAmount();
            BigDecimal rate = interest.getRate();
            LocalDate fromDate = getFromDate(interest, issuedOn);
            return calculateInterest(claimAmount, rate, fromDate, toDate);
        }

        return ZERO;
    }

    private static BigDecimal calculateTotalAmount(Amount amount, Interest interest, LocalDate toDate, LocalDate issuedOn) {
        if (amount instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) amount).getTotalAmount();

            BigDecimal calculatedInterest = calculateInterest(amount, interest, toDate, issuedOn).orElse(ZERO);

            return claimAmount.add(calculatedInterest);
        }

        return null;
    }

    public static Optional<BigDecimal> amountWithInterest(Claim claim) {
        LocalDate date = getDateApplicable(claim);

        return Optional.ofNullable(calculateTotalAmount(claim, date, false));
    }

    public static Optional<BigDecimal> amountWithInterestUntilIssueDate(Claim claim) {
        return Optional.ofNullable(calculateTotalAmount(claim, claim.getIssuedOn(), false));
    }

    public static Optional<BigDecimal> totalTillToday(Claim claim) {
        LocalDate date = getDateApplicable(claim);

        return Optional.ofNullable(calculateTotalAmount(claim, date, true));

    }

    public static Optional<BigDecimal> totalTillDateOfIssue(Claim claim) {
        return Optional.ofNullable(calculateTotalAmount(claim, claim.getIssuedOn(), true));
    }

    public static Optional<BigDecimal> calculateInterestForClaim(Claim claim, LocalDate localDate) {
        return calculateInterest(claim, localDate);
    }

    public static Optional<BigDecimal> calculateInterestForClaim(Claim claim) {
        return calculateInterestForClaim(claim, getToDate(claim));
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

    private static Optional<BigDecimal> calculateInterest(Claim claim, LocalDate toDate) {
        return calculateInterest(
            claim.getClaimData().getAmount(),
            claim.getClaimData().getInterest(),
            toDate,
            claim.getIssuedOn()
        );
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

    public static BigDecimal calculateBreakdownInterest(
        Interest interest,
        InterestDate interestDate,
        BigDecimal claimAmount,
        LocalDate issuedOn,
        LocalDate toDate
    ) {
        BigDecimal accruedInterest = ZERO;

        if (interestDate.isEndDateOnClaimComplete()) {
            accruedInterest = calculateInterest(
                interest.getSpecificDailyAmount()
                    .orElseGet(() -> calculateDailyAmount(claimAmount, interest.getRate())),
                daysBetween(issuedOn, toDate)
            );
        }
        BigDecimal interestValue = interest.getInterestBreakdown().getTotalAmount();

        return interestValue.add(accruedInterest);
    }

    public static BigDecimal asFraction(BigDecimal interestRate) {
        requireNonNegative(interestRate);

        return interestRate.divide(HUNDRED, DIVISION_DECIMAL_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateTotalAmount(Claim claim, LocalDate toDate, boolean withFees) {
        ClaimData data = claim.getClaimData();

        if (data.getAmount() instanceof AmountBreakDown) {
            BigDecimal claimAmount = ((AmountBreakDown) data.getAmount()).getTotalAmount();

            BigDecimal interest = calculateInterest(claim, toDate).orElse(ZERO);

            if (withFees) {
                BigDecimal feesPaid = data.getFeesPaidInPounds();
                return claimAmount.add(interest.add(feesPaid));
            } else {
                return claimAmount.add(interest);
            }

        }

        return null;
    }

    private static LocalDate getFromDate(Interest interest, LocalDate issuedOn) {
        return interest.getInterestDate().isCustom()
            ? interest.getInterestDate().getDate()
            : issuedOn;
    }

    private static LocalDate getToDate(Claim claim) {
        if (claim.getCountyCourtJudgmentRequestedAt() != null) {
            return claim.getCountyCourtJudgmentRequestedAt().toLocalDate();
        } else {
            return LocalDate.now().isAfter(claim.getIssuedOn()) ? LocalDate.now() : claim.getIssuedOn();
        }
    }

    private static BigDecimal daysBetween(LocalDate startDate, LocalDate endDate) {
        // This should be enabled back and fixed properly
        // requireValidOrderOfDates(startDate, endDate);
        Duration duration = Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay());

        return duration.isNegative() ? ZERO : valueOf(duration.toDays());
    }

    private static void requireNonNegative(BigDecimal value) {
        requireNonNull(value);

        if (value.signum() == -1) {
            throw new IllegalArgumentException("Expected non-negative number");
        }
    }

    private static LocalDate getDateApplicable(Claim claim) {
        return claim.getCountyCourtJudgmentRequestedAt() == null ? LocalDate.now()
            : claim.getCountyCourtJudgmentRequestedAt().toLocalDate();
    }
}
