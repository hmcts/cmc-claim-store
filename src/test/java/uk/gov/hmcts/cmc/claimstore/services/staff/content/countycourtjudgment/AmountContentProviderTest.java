package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AmountContentProviderTest {

    private AmountContentProvider provider;

    @Before
    public void beforeEachTest() {
        provider = new AmountContentProvider(
            new InterestContentProvider(
                new InterestCalculationService(Clock.systemUTC())
            )
        );
    }

    private Claim noInterest = SampleClaim.builder()
        .withClaimData(SampleClaimData.noInterest())
        .withCountyCourtJudgment(SampleCountyCourtJudgment.builder().build())
        .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
        .build();

    @Test
    public void calculateWithNoPaidAmount() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(null).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(provider.create(claim).getRemainingAmount())
            .isEqualTo("£80.89");
    }

    @Test
    public void calculateWithNoInterest() {
        assertThat(provider.create(noInterest).getRemainingAmount())
            .isEqualTo("£80.00");
    }

    @Test
    public void calculate() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .withPaidAmount(BigDecimal.valueOf(10)).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(provider.create(claim).getRemainingAmount())
            .isEqualTo("£70.89");
    }

}
