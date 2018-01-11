package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AmountContentProviderTest {

    private InterestCalculationService interestCalculationService = new InterestCalculationService(Clock.systemUTC());

    private Claim noInterest = SampleClaim.builder()
        .withClaimData(SampleClaimData.noInterest())
        .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
        .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    @Test
    public void calculateWithNoPaidAmount() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(null).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(new AmountContentProvider(interestCalculationService).create(claim).getRemainingAmount())
            .isEqualTo("£80.89");
    }

    @Test
    public void calculateWithNoInterest() {
        assertThat(new AmountContentProvider(interestCalculationService).create(noInterest).getRemainingAmount())
            .isEqualTo("£80.00");
    }

    @Test
    public void calculate() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.submittedByClaimant())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(BigDecimal.valueOf(10)).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(new AmountContentProvider(interestCalculationService).create(claim).getRemainingAmount())
            .isEqualTo("£70.89");
    }

    @Test
    public void interestShouldSayNoInterestIfNoneClaimed() {
        assertThat(new AmountContentProvider(interestCalculationService).create(noInterest).getInterest().getFromDate())
            .isEqualTo("No interest claimed");
    }
}
