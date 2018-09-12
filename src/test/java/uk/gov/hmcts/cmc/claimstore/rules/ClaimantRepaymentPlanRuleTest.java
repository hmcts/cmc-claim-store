package uk.gov.hmcts.cmc.claimstore.rules;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantInvalidRepaymentPlanException;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PaymentIntention;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleRepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleResponse;
import uk.gov.hmcts.cmc.domain.models.sampledata.response.SamplePaymentIntention;

import java.time.LocalDate;

@RunWith(MockitoJUnitRunner.class)
public class ClaimantRepaymentPlanRuleTest {

    private ClaimantRepaymentPlanRule claimantRepaymentPlanRule = new ClaimantRepaymentPlanRule();

    private final PaymentIntention installmentPaymentIntentionBeforeOneMonth = SamplePaymentIntention.builder()
        .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusDays(3)).build())
        .build();

    private final PaymentIntention installmentPaymentIntentionAfterOneMonth = SamplePaymentIntention.builder()
        .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusMonths(2)).build())
        .build();


    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldFailValidationWhenDateIsEarlierThanOneMonthAndDefendantPlanIsInstallments() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldPassValidationWhenDateIsLaterThanOneMonthAndDefendantPlanIsInstallments() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusMonths(2)).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldNotValidateWhenInstallmentsDefendantDateIsOverOneMonthAndClaimantDateIsBeforeOneMonth() {
        Claim claim = SampleClaim.getWithResponse(
            FullAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionAfterOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldValidateWhenInstallmentsDefendantDateIsOverOneMonthAndClaimantDateIsOverOneMonth() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionAfterOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusMonths(1).plusDays(1)).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldFailWhenInstallmentsAndDefendantDateIsBeforeOneMonthAndClaimantDateIsBeforeDefendantDate() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusDays(2)).build());
    }

    @Test
    public void shouldValidateWhenInstallmentsAndDefendantDateIsBeforeOneMonthAndClaimantDateIsDefendantDate() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now().plusDays(4)).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldThrowExceptionWhenClaimWithNoResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(null);

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldThrowExceptionWhenClaimWithFullDefenseResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(SampleResponse.validDefaults());

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());

    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldThrowExceptionWhenClaimWithImmediatePaymentResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(SamplePaymentIntention.immediately()).build());

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldThrowExceptionWhenPartAdmissionResponseIsMissingPaymentIntention() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(null).build());

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }

    @Test(expected = ClaimantInvalidRepaymentPlanException.class)
    public void shouldThrowExceptionWhenFullAdmissionResponseWithInstallmentsIntentionHasNoRepaymentPlan() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(
                SamplePaymentIntention.builder().paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(null).build()
            ).build());

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().withFirstPaymentDate(LocalDate.now()).build());
    }
}
