package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AmountContentProviderTest {

    private InterestCalculationService interestCalculationService = new InterestCalculationService(Clock.systemUTC());

    @Test
    public void calculateWithNoPaidAmount() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(null).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(AmountRemainingContentProvider.calculate(interestCalculationService, claim)).isEqualTo("£40.88");
    }

    @Test
    public void calculateWithNoInterest() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.noInterest())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(AmountRemainingContentProvider.calculate(interestCalculationService, claim)).isEqualTo("£40.00");
    }

    @Test
    public void calculate() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(BigDecimal.valueOf(10)).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(AmountRemainingContentProvider.calculate(interestCalculationService, claim)).isEqualTo("£30.66");
    }

}
