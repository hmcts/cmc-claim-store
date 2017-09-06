package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.controllers.utils.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.ISSUE_DATE;
import static uk.gov.hmcts.cmc.claimstore.utils.DatesProvider.NOW_IN_LOCAL_ZONE;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDateTime;

public class ClaimContentProviderTest {

    private Claim claim = SampleClaim.getDefault();

    private ClaimContentProvider provider = new ClaimContentProvider(
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
    public void shouldProvideTotalAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.88");
    }

    @Test
    public void shouldUseOnlyAmountAndFeeForTotalIfSubmissionInterestDateIsGiven() {
        ClaimContent claimContent = provider.createContent(SampleClaim.getWithSubmissionInterestDate());

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.00");
    }

}
