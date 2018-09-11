package uk.gov.hmcts.cmc.claimstore.rules;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cmc.claimstore.exceptions.ClaimantInvalidRepaymentPlanException;
import uk.gov.hmcts.cmc.claimstore.utils.CalculateMonthIncrement;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.PaymentOption;
import uk.gov.hmcts.cmc.domain.models.RepaymentPlan;
import uk.gov.hmcts.cmc.domain.models.response.FullAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.PartAdmissionResponse;
import uk.gov.hmcts.cmc.domain.models.response.Response;

import java.time.LocalDate;

@Service
public class ClaimantRepaymentPlanRule {

    public void assertClaimantRepaymentPlanIsValid(Claim claim, RepaymentPlan proposedPlan) {
        Response response = claim.getResponse().orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
            ClaimantInvalidRepaymentPlanException.EXPECTED_DEFENDANT_RESPONSE));

        switch (getPaymentOptionForResponse(response)) {
            case INSTALMENTS:
                assertInstallmentsIsValidAgainstInstallments(proposedPlan, getRepaymentPlanForResponse(response));
                break;
            case BY_SPECIFIED_DATE:
                assertInstallmentsIsValidAgainstSetDate(proposedPlan);
                break;
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    ClaimantInvalidRepaymentPlanException.INVALID_DEFENDANT_REPAYMENT_TYPE);
        }
    }


    private void assertInstallmentsIsValidAgainstInstallments(RepaymentPlan claimantPlan, RepaymentPlan defendantPlan) {
        LocalDate defendantStartDate = defendantPlan.getFirstPaymentDate();
        LocalDate oneMonthForNow = CalculateMonthIncrement.calculateMonthlyIncrement(LocalDate.now());
        LocalDate thresholdDate = defendantStartDate.isBefore(oneMonthForNow) ? defendantStartDate : oneMonthForNow;

        if (claimantPlan.getFirstPaymentDate().isBefore(thresholdDate)) {
            throw new ClaimantInvalidRepaymentPlanException(
                "Claimant first installment date must be before " + thresholdDate);
        }
    }

    private void assertInstallmentsIsValidAgainstSetDate(RepaymentPlan claimantPlan) {
        LocalDate oneMonthFromNow = CalculateMonthIncrement.calculateMonthlyIncrement(LocalDate.now());
        if (claimantPlan.getFirstPaymentDate().isBefore(oneMonthFromNow)) {
            throw new ClaimantInvalidRepaymentPlanException(
                "Claimant first installment date must be at least one month from now");
        }
    }

    private PaymentOption getPaymentOptionForResponse(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response).getPaymentIntention()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        ClaimantInvalidRepaymentPlanException.EXPECTED_PAYMENT_INTENTION))
                    .getPaymentOption();
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response).getPaymentIntention().getPaymentOption();
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    ClaimantInvalidRepaymentPlanException.INVALID_RESPONSE_TYPE);
        }
    }

    private RepaymentPlan getRepaymentPlanForResponse(Response response) {
        switch (response.getResponseType()) {
            case PART_ADMISSION:
                return ((PartAdmissionResponse) response)
                    .getPaymentIntention()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        ClaimantInvalidRepaymentPlanException.EXPECTED_PAYMENT_INTENTION))
                    .getRepaymentPlan()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        ClaimantInvalidRepaymentPlanException.EXPECTED_REPAYMENT_PLAN));
            case FULL_ADMISSION:
                return ((FullAdmissionResponse) response)
                    .getPaymentIntention()
                    .getRepaymentPlan()
                    .orElseThrow(() -> new ClaimantInvalidRepaymentPlanException(
                        ClaimantInvalidRepaymentPlanException.EXPECTED_REPAYMENT_PLAN));
            default:
                throw new ClaimantInvalidRepaymentPlanException(
                    ClaimantInvalidRepaymentPlanException.INVALID_RESPONSE_TYPE);

        }
    }
}


