package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.documents.ClaimDataContentProvider;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.ClaimContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.legalrep.StatementOfTruth;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;

import java.time.Clock;
import java.util.List;

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
    public void shouldProvideExpectedReasonWithoutDelimiters() {
        testReason("reason one reason two", "reason one reason two");
    }

    @Test
    public void shouldProvideReasonHonouringInputParagraphs() {
        testReason("'Twas brillig and the slithy toves\ndid gyre and gimble in the wabe\r\n"
                + "All mimsy were the borogoves\rand the mome raths outgrabe",

            "'Twas brillig and the slithy toves", "did gyre and gimble in the wabe",
            "All mimsy were the borogoves", "and the mome raths outgrabe");
    }

    @Test
    public void shouldProvideExpectedClaimAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getClaimAmount()).isEqualTo("£40.99");
    }

    @Test
    public void shouldProvideExpectedFeeAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getFeeAmount()).isEqualTo("£40");
    }

    @Test
    public void shouldProvideClaimInterest() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getInterest()).isNotNull();
    }

    @Test
    public void shouldProvideTotalAmount() {
        ClaimContent claimContent = provider.createContent(claim);

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£81.91");
    }

    @Test
    public void shouldUseOnlyAmountAndFeeForTotalIfSubmissionInterestDateIsGiven() {
        ClaimContent claimContent = provider.createContent(SampleClaim.getWithSubmissionInterestDate());

        assertThat(claimContent.getClaimTotalAmount()).isEqualTo("£80.99");
    }

    @Test
    public void shouldProvideCompanyStatementOfSignerName() {
        Claim claim = SampleClaim.builder()
                .withClaimData(SampleClaimData.builder().withStatementOfTruth(StatementOfTruth.builder()
                        .signerName("Jana").build()).build())
                .build();
        ClaimContent claimContent = provider.createContent(claim);
        assertThat(claimContent.getStatementOfTruth().getSignerName()).containsSequence("Jana");
    }

    @Test
    public void shouldProvideCompanyStatementOfTruthSignerRole() {
        Claim claim = SampleClaim.builder()
                .withClaimData(SampleClaimData.builder().withStatementOfTruth(StatementOfTruth.builder()
                        .signerRole("Director").build()).build())
                .build();
        ClaimContent claimContent = provider.createContent(claim);
        assertThat(claimContent.getStatementOfTruth().getSignerRole()).containsSequence("Director");
    }

    private void testReason(String inputReason, String... expectations) {
        Claim claim = SampleClaim.builder()
            .withClaimData(SampleClaimData.builder().withReason(inputReason).build())
            .build();
        ClaimContent claimContent = provider.createContent(claim);
        List<String> reason = claimContent.getReason();
        assertThat(reason).containsSequence(expectations);
    }
}
