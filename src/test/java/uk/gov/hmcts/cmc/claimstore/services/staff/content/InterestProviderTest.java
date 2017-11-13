package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.models.Claim;
import uk.gov.hmcts.cmc.claimstore.models.Interest;
import uk.gov.hmcts.cmc.claimstore.models.InterestDate;
import uk.gov.hmcts.cmc.claimstore.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.claimstore.model.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class InterestProviderTest {

    private Claim claim = SampleClaim.getDefault();

    private Interest interest;
    private InterestDate interestDate;
    private BigDecimal claimAmount;
    private LocalDateTime submittedOn;

    private InterestContentProvider provider = new InterestContentProvider(
        new InterestCalculationService(Clock.systemDefaultZone())
    );

    @Before
    public void beforeEachTest() {
        interest = claim.getClaimData().getInterest();
        interestDate = claim.getClaimData().getInterestDate();
        claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        submittedOn = claim.getCreatedAt();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullInterest() {
        provider.createContent(null, interestDate, claimAmount, submittedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullInterestDate() {
        provider.createContent(interest, null, claimAmount, submittedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaimAmount() {
        provider.createContent(interest, interestDate, null, submittedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullSubmittedOn() {
        provider.createContent(interest, interestDate, claimAmount, null);
    }

    @Test
    public void shouldProvideExpectedRate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.getRate()).isEqualTo("8%");
    }

    @Test
    public void shouldNotClaimCustomRate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.isCustomRate()).isFalse();
    }

    @Test
    public void customRateReasonShouldBeBlank() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.getCustomRateReason()).isNullOrEmpty();
    }

    @Test
    public void shouldClaimFromCustomDate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.isCustomFromDate()).isTrue();
    }

    @Test
    public void shouldProvideInterestFromDate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.getFromDate()).isEqualTo(formatDate(interestDate.getDate()));
    }

    @Test
    public void shouldProvideDailyAmount() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, submittedOn);

        assertThat(content.getDailyAmount()).isEqualTo("£0.01");
    }

    @Test
    public void shouldProvideAmountUpToNow() {
        InterestContent content = provider.createContent(interest, hundredOneDaysAgo(), claimAmount, submittedOn);

        assertThat(content.getAmount()).isEqualTo("£0.88");
    }

    private InterestDate hundredOneDaysAgo() {
        return new InterestDate(
                InterestDate.InterestDateType.CUSTOM,
                LocalDate.now().minusDays(101),
                "testing");
    }

    @Test
    public void shouldProvideAmountUpToNowRealValue() {
        InterestContent content = provider.createContent(interest, hundredOneDaysAgo(), claimAmount, submittedOn);

        assertThat(content.getAmountRealValue()).isEqualByComparingTo("0.88");
    }

    @Test
    public void customInterestDateShouldBeFalseIfSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, submissionDate(), claimAmount, submittedOn);

        assertThat(content.isCustomFromDate()).isFalse();
    }

    private InterestDate submissionDate() {
        return new InterestDate(
            InterestDate.InterestDateType.SUBMISSION,
            claim.getCreatedAt().toLocalDate(),
            "testing"
        );
    }

    @Test
    public void amountUpToNowShouldBeNullWhenSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, submissionDate(), claimAmount, submittedOn);

        assertThat(content.getAmount()).isNull();
        assertThat(content.getAmountRealValue()).isNull();
    }

    @Test
    public void dateFromShouldBeClaimCreatedOnWhenSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, submissionDate(), claimAmount, submittedOn);

        assertThat(content.getFromDate()).isEqualTo(formatDate(submittedOn));
    }

}
