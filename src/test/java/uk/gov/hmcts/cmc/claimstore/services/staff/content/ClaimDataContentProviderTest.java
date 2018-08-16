package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.domain.models.AmountRow;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestBreakdown;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleInterest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.domain.utils.DatesProvider.NOW_IN_LOCAL_ZONE;

public class ClaimDataContentProviderTest {

    private Claim claim = SampleClaim.getDefault();

    private ClaimDataContentProvider provider = new ClaimDataContentProvider(
        new InterestContentProvider(
            new InterestCalculationService(Clock.systemDefaultZone())
        )
    );

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaim() {
        provider.createContent(null);
    }

    @Test
    public void shouldProvideExpectedSubmittedOn() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getSubmittedOn()).isEqualTo(formatDateTime(NOW_IN_LOCAL_ZONE));
    }

    @Test
    public void shouldProvideExpectedIssuedOn() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getIssuedOn()).matches(formatDate(ISSUE_DATE));
    }

    @Test
    public void shouldProvideExpectedReason() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getReason()).isEqualTo(claim.getClaimData().getReason());
    }

    @Test
    public void shouldProvideExpectedClaimAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getClaimAmount()).isEqualTo("£40.00");
    }

    @Test
    public void shouldProvideExpectedFeeAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getFeeAmount()).isEqualTo("£40.00");
    }

    @Test
    public void shouldProvideClaimInterest() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getInterest()).isNotNull();
    }

    @Test
    public void shouldProvideCorrectClaimTotalWithInterest() {
        LocalDate yesterday = LocalDate.now().minus(Period.ofDays(1));
        Interest interest = SampleInterest.builder()
            .withInterestDate(
                new InterestDate(InterestDate.InterestDateType.SUBMISSION,
                    LocalDate.now().minus(Period.ofDays(1)),
                    "reason",
                    InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT))
            .withType(Interest.InterestType.DIFFERENT)
            .build();

        Claim oneDayInterestClaim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder()
                .withInterest(interest)
                .build())
            .withIssuedOn(yesterday)
            .withCreatedAt(yesterday.atStartOfDay())
            .build();

        ClaimContent claimContent = provider.createContent(oneDayInterestClaim);

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.01");
    }

    @Test
    public void shouldProvideTotalAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.89");
    }

    @Test
    public void shouldUseOnlyAmountAndFeeForTotalIfSubmissionInterestDateIsGiven() {
        ClaimContent claimContent = provider.createContent(SampleClaim.getWithSubmissionInterestDate());

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.00");
    }

}
