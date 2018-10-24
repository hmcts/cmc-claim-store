package uk.gov.hmcts.cmc.claimstore.services.staff.content.countycourtjudgment;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.content.InterestContentProvider;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.response.Response;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleCountyCourtJudgment;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AmountContentProviderTest {

    private AmountContentProvider amountContentProvider;

    @Before
    public void beforeEachTest() {
        amountContentProvider = new AmountContentProvider(
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
                .paidAmount(null).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(amountContentProvider.create(claim).getRemainingAmount())
            .isEqualTo("£80.89");
    }

    @Test
    public void calculateAmountDetailsWithPartAdmissions() {
        Response partAdmissionsResponse = SampleResponse.PartAdmission.builder().build();
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .paidAmount(null).build())
            .withResponse(partAdmissionsResponse)
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        AmountContent amountContent = amountContentProvider.create(claim);
        assertThat(amountContent.getAdmittedAmount()).isEqualTo("£120.00");
        assertThat(amountContent.getSubTotalAmount()).isEqualTo("£160.00");
        assertThat(amountContent.getRemainingAmount()).isEqualTo("£160.00");
    }

    @Test
    public void calculateAmountDetailsWithPartAdmissionsWithPaidAmount() {
        Response partAdmissionsResponse = SampleResponse.PartAdmission.builder().build();
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .paidAmount(BigDecimal.TEN).build())
            .withResponse(partAdmissionsResponse)
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        AmountContent amountContent = amountContentProvider.create(claim);
        assertThat(amountContent.getAdmittedAmount()).isEqualTo("£120.00");
        assertThat(amountContent.getSubTotalAmount()).isEqualTo("£160.00");
        assertThat(amountContent.getRemainingAmount()).isEqualTo("£150.00");
    }

    @Test
    public void calculateWithNoInterest() {
        assertThat(amountContentProvider.create(noInterest).getRemainingAmount())
            .isEqualTo("£80.00");
    }

    @Test
    public void calculate() {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.validDefaults())
            .withCountyCourtJudgment(SampleCountyCourtJudgment.builder()
                .paidAmount(BigDecimal.valueOf(10)).build())
            .withCountyCourtJudgmentRequestedAt(LocalDateTime.now())
            .build();

        assertThat(amountContentProvider.create(claim).getRemainingAmount())
            .isEqualTo("£70.89");
    }

}
