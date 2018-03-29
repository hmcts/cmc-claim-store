package uk.gov.hmcts.cmc.domain.amount;

import org.junit.Test;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountBreakdown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleAmountRange;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterestDate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TotalAmountCalculatorTest {

    private static BigDecimal ZERO = format(BigDecimal.ZERO);
    private static LocalDate TODAY = LocalDate.now();
    private static BigInteger TWENTY_POUNDS_IN_PENNIES = BigInteger.valueOf(2000);

    @Test
    public void calculateInterestShouldReturn0WhenAmountIs0() {
        assertThat(
            TotalAmountCalculator.calculateInterest(
                ZERO,
                new BigDecimal("1000"),
                TODAY.minusDays(1000),
                TODAY
            )
        ).isEqualTo(ZERO);
    }

    @Test
    public void calculateInterestShouldReturn0WhenInterestIs0() {
        BigDecimal amount = new BigDecimal("1000");
        assertThat(
            TotalAmountCalculator.calculateInterest(
                amount,
                ZERO,
                TODAY.minusDays(1000),
                TODAY
            )
        ).isEqualTo(ZERO);
    }

    @Test
    public void calculateInterestShouldReturnCalculatedValidValue() {
        BigDecimal amount = new BigDecimal("1000");
        assertThat(
            TotalAmountCalculator.calculateInterest(
                amount,
                new BigDecimal("8"),
                TODAY.minusDays(1000),
                TODAY
            )
        ).isEqualTo(format(new BigDecimal("219.18")));
    }

    @Test
    public void totalTillTodayShouldReturnEmptyOptionalWhenClaimHasAmountDifferentThanAmountBreakdown() {
        assertThat(TotalAmountCalculator.totalTillToday(claimWithAmountRange())).isEqualTo(Optional.empty());
    }

    @Test
    public void totalTillTodayShouldReturnClaimAmountPlusFeeWhenNoInterest() {
        Claim claimNoInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withInterest(SampleInterest.noInterest())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .build()
            ).build();

        assertThat(TotalAmountCalculator.totalTillToday(claimNoInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60"))));
    }

    @Test
    public void totalTillDateOfIssueShouldReturnClaimAmountPlusFeeWhenNoInterest() {
        Claim claimNoInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withInterest(SampleInterest.noInterest())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .build()
            ).build();

        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claimNoInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60"))));
    }

    @Test
    public void totalTillTodayShouldReturnCalculatedTotalAmountPlusInterestWhenCalculateFromIssuedOn() {

        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            ).build();

        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claimStandardInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60"))));
    }

    @Test
    public void totalTillTodayShouldNotIncludeInterestWhenClaimIssuedOnIsToday() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now())
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claimStandardInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60"))));
    }

    @Test
    public void totalTillTodayShouldNotIncludeInterestWhenClaimIssuedOnIsTomorrow() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().plusDays(1))
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claimStandardInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60"))));
    }

    @Test
    public void totalTillTodayShouldIncludeOneDayInterestWhenClaimIssuedOnIsYesterday() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(1))
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claimStandardInterest))
            .isEqualTo(Optional.of(format(new BigDecimal("60.01"))));
    }

    @Test
    public void totalTillDateOfIssueShouldReturnEmptyOptionalWhenClaimHasAmountDifferentThanAmountBreakdown() {
        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claimWithAmountRange())).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldCalculateTotalAmountTillTodayForBreakdownInterestWithoutContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.breakdownOnly())
                    .withInterestDate(SampleInterestDate.submissionToSubmission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void shouldCalculateTotalAmountTillTodayForBreakdownInterestWithFixedAmountContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withSpecificDailyAmount(new BigDecimal("10"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("130.00"));
    }

    @Test
    public void shouldCalculateTotalAmountTillTodayForBreakdownInterestWithRateContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withRate(new BigDecimal("8.00"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.totalTillToday(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("100.03"));
    }

    @Test
    public void shouldCalculateTotalAmountTillDateOfIssueForBreakdownInterestWithoutContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.breakdownOnly())
                    .withInterestDate(SampleInterestDate.submissionToSubmission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(1))
            .build();

        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void shouldCalculateTotalAmountTillDateOfIssueForBreakdownInterestWithFixedAmountContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withSpecificDailyAmount(new BigDecimal("10"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void shouldCalculateTotalAmountTillDateOfIssueForBreakdownInterestWithRateContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withRate(new BigDecimal("8.00"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.totalTillDateOfIssue(claim))
            .isPresent()
            .get()
            .isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void totalInterestShouldBeZeroWhenClaimIssuedOnIsToday() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now())
            .build();

        assertThat(TotalAmountCalculator.calculateInterestForClaim(claimStandardInterest))
            .isEqualTo(format(new BigDecimal("0.00")));
    }

    @Test
    public void totalInterestShouldBeZeroWhenClaimIssuedOnIsTomorrow() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().plusDays(1))
            .build();

        assertThat(TotalAmountCalculator.calculateInterestForClaim(claimStandardInterest))
            .isEqualTo(format(new BigDecimal("0.00")));
    }

    @Test
    public void totalInterestShouldIncludeOneDayInterestWhenClaimIssuedOnIsYesterday() {
        Claim claimStandardInterest = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(SampleInterest.standard())
                    .withInterestDate(SampleInterestDate.submission())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(1))
            .build();

        assertThat(TotalAmountCalculator.calculateInterestForClaim(claimStandardInterest))
            .isEqualTo(format(new BigDecimal("0.01")));
    }

    @Test
    public void shouldCalculateTotalInterestForBreakdownInterestWithFixedAmountContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withSpecificDailyAmount(new BigDecimal("10"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(3))
            .build();

        assertThat(TotalAmountCalculator.calculateInterestForClaim(claim))
            .isEqualTo(format(new BigDecimal("70.00")));
    }

    @Test
    public void shouldCalculateTotalInterestForBreakdownInterestWithRateContinuingInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder()
                    .withAmount(SampleAmountBreakdown.validDefaults())
                    .withFeeAmount(TWENTY_POUNDS_IN_PENNIES)
                    .withInterest(
                        SampleInterest.breakdownInterestBuilder()
                            .withRate(new BigDecimal("8.00"))
                            .build()
                    )
                    .withInterestDate(SampleInterestDate.submissionToSettledOrJudgement())
                    .build()
            )
            .withIssuedOn(LocalDate.now().minusDays(2))
            .build();

        assertThat(TotalAmountCalculator.calculateInterestForClaim(claim))
            .isEqualTo(format(new BigDecimal("40.02")));
    }


    private static Claim claimWithAmountRange() {
        return SampleClaim.builder()
            .withClaimData(
                SampleClaimData.builder().withAmount(SampleAmountRange.validDefaults()).build()
            ).build();
    }

    private static BigDecimal format(BigDecimal value) {
        return value.setScale(TotalAmountCalculator.TO_FULL_PENNIES, RoundingMode.HALF_UP);
    }
}
