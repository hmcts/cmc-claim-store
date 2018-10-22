package uk.gov.hmcts.cmc.claimstore.rules;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ClaimantRepaymentPlanRule claimantRepaymentPlanRule = new ClaimantRepaymentPlanRule();

    private final PaymentIntention installmentPaymentIntentionBeforeOneMonth = SamplePaymentIntention.builder()
        .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusDays(3)).build())
        .build();

    private final PaymentIntention installmentPaymentIntentionAfterOneMonth = SamplePaymentIntention.builder()
        .paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusMonths(2)).build())
        .build();


    @Test
    public void shouldFailValidationWhenDateIsEarlierThanOneMonthAndDefendantPlanIsInstallments() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );
        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(StringContains.containsString(
            String.format(ClaimantRepaymentPlanRule.INSTALLMENT_DATE_MUST_BE_AFTER, "")));

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());

    }

    @Test
    public void shouldPassValidationWhenDateIsLaterThanOneMonthAndDefendantPlanIsInstallments() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusMonths(2)).build());
    }

    @Test
    public void shouldNotValidateWhenInstallmentsDefendantDateIsOverOneMonthAndClaimantDateIsBeforeOneMonth() {
        Claim claim = SampleClaim.getWithResponse(
            FullAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionAfterOneMonth).build()
        );

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(String.format(ClaimantRepaymentPlanRule.INSTALLMENT_DATE_MUST_BE_AFTER, "")));

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldValidateWhenInstallmentsDefendantDateIsOverOneMonthAndClaimantDateIsOverOneMonth() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionAfterOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusMonths(1).plusDays(1)).build());
    }

    @Test
    public void shouldFailWhenInstallmentsAndDefendantDateIsBeforeOneMonthAndClaimantDateIsBeforeDefendantDate() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(String.format(ClaimantRepaymentPlanRule.INSTALLMENT_DATE_MUST_BE_AFTER, "")));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusDays(2)).build());
    }

    @Test
    public void shouldValidateWhenInstallmentsAndDefendantDateIsBeforeOneMonthAndClaimantDateIsDefendantDate() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(installmentPaymentIntentionBeforeOneMonth).build()
        );

        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now().plusDays(4)).build());
    }

    @Test
    public void shouldThrowExceptionWhenClaimWithNoResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(null);

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(String.format(ClaimantRepaymentPlanRule.EXPECTED_DEFENDANT_RESPONSE, "")));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldThrowExceptionWhenClaimWithFullDefenseResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(SampleResponse.validDefaults());

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(StringContains.containsString(ClaimantRepaymentPlanRule.INVALID_RESPONSE_TYPE));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());

    }

    @Test
    public void shouldThrowExceptionWhenClaimWithImmediatePaymentResponseIsGiven() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(SamplePaymentIntention.immediately()).build());

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(ClaimantRepaymentPlanRule.INVALID_DEFENDANT_REPAYMENT_TYPE));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldThrowExceptionWhenPartAdmissionResponseIsMissingPaymentIntention() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(null).build());

        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(ClaimantRepaymentPlanRule.EXPECTED_PAYMENT_INTENTION));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());
    }

    @Test
    public void shouldThrowExceptionWhenFullAdmissionResponseWithInstallmentsIntentionHasNoRepaymentPlan() {
        Claim claim = SampleClaim.getWithResponse(
            PartAdmissionResponse.builder().paymentIntention(
                SamplePaymentIntention.builder().paymentOption(PaymentOption.INSTALMENTS).repaymentPlan(null).build()
            ).build());


        expectedException.expect(ClaimantInvalidRepaymentPlanException.class);
        expectedException.expectMessage(
            StringContains.containsString(ClaimantRepaymentPlanRule.EXPECTED_REPAYMENT_PLAN_DEFENDANT));
        claimantRepaymentPlanRule.assertClaimantRepaymentPlanIsValid(claim,
            SampleRepaymentPlan.builder().firstPaymentDate(LocalDate.now()).build());
    }
}
