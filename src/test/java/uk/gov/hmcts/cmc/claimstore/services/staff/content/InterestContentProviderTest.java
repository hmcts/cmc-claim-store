package uk.gov.hmcts.cmc.claimstore.services.staff.content;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.cmc.claimstore.services.interest.InterestCalculationService;
import uk.gov.hmcts.cmc.claimstore.services.staff.models.InterestContent;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.Interest;
import uk.gov.hmcts.cmc.domain.models.InterestDate;
import uk.gov.hmcts.cmc.domain.models.amount.AmountBreakDown;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.Formatting.formatDate;

public class InterestContentProviderTest {

    private final Claim claim = SampleClaim.getDefault();

    private Interest interest;
    private InterestDate interestDate;
    private BigDecimal claimAmount;
    private LocalDate issuedOn;

    private final InterestContentProvider provider = new InterestContentProvider(
        new InterestCalculationService(Clock.systemDefaultZone())
    );

    @Before
    public void beforeEachTest() {
        interest = claim.getClaimData().getInterest();
        interestDate = claim.getClaimData().getInterest().getInterestDate();
        claimAmount = ((AmountBreakDown) claim.getClaimData().getAmount()).getTotalAmount();
        issuedOn = claim.getIssuedOn();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullInterest() {
        provider.createContent(null, interestDate, claimAmount, issuedOn, issuedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullInterestDate() {
        provider.createContent(interest, null, claimAmount, issuedOn, issuedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullClaimAmount() {
        provider.createContent(interest, interestDate, null, issuedOn, issuedOn);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerForNullIssuedOn() {
        provider.createContent(interest, interestDate, claimAmount, null, null);
    }

    @Test
    public void shouldProvideExpectedRate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.getRate()).isEqualTo("8%");
    }

    @Test
    public void shouldNotClaimCustomRate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.isCustomRate()).isFalse();
    }

    @Test
    public void customRateReasonShouldBeBlank() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.getCustomRateReason()).isNullOrEmpty();
    }

    @Test
    public void shouldClaimFromCustomDate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.isCustomFromDate()).isTrue();
    }

    @Test
    public void shouldProvideInterestFromDate() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.getFromDate()).isEqualTo(formatDate(interestDate.getDate()));
    }

    @Test
    public void shouldProvideDailyAmount() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.getDailyAmount()).isEqualTo("£0.01");
    }

    @Test
    public void shouldProvideInterestRateReason() {
        InterestContent content = provider.createContent(interest, interestDate, claimAmount, issuedOn, issuedOn);

        assertThat(content.getStartDateReason())
            .isEqualTo("I want to claim from this date because that's when that happened");
    }

    @Test
    public void shouldProvideAmountUpToNow() {
        InterestContent content = provider.createContent(interest, hundredOneDaysAgo(), claimAmount,
            issuedOn, issuedOn);

        assertThat(content.getAmount()).isEqualTo("£0.92");
    }

    private InterestDate hundredOneDaysAgo() {
        return new InterestDate(
                InterestDate.InterestDateType.CUSTOM,
                LocalDate.now().minusDays(101),
                "testing",
                InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT);
    }

    @Test
    public void shouldProvideAmountUpToNowRealValue() {
        InterestContent content = provider.createContent(interest, hundredOneDaysAgo(), claimAmount,
            issuedOn, issuedOn);

        assertThat(content.getAmountRealValue()).isEqualByComparingTo("0.92");
    }

    @Test
    public void customInterestDateShouldBeFalseIfSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, issuedOnDate(), claimAmount, issuedOn, issuedOn);

        assertThat(content.isCustomFromDate()).isFalse();
    }

    private InterestDate issuedOnDate() {
        return new InterestDate(
            InterestDate.InterestDateType.SUBMISSION,
            claim.getIssuedOn(),
            "testing",
            InterestDate.InterestEndDateType.SETTLED_OR_JUDGMENT
        );
    }

    @Test
    public void amountUpToNowShouldBeZeroWhenSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, issuedOnDate(), claimAmount, issuedOn, issuedOn);

        assertThat(content.getAmount()).isEqualTo("£0");
        assertThat(content.getAmountRealValue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    public void dateFromShouldBeClaimCreatedOnWhenSubmissionDateIsUsed() {
        InterestContent content = provider.createContent(interest, issuedOnDate(), claimAmount, issuedOn, issuedOn);

        assertThat(content.getFromDate()).isEqualTo(formatDate(issuedOn));
    }

}
